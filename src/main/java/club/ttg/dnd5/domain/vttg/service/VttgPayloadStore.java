package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.model.VttgExport;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import club.ttg.dnd5.domain.vttg.repository.VttgExportRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Хранилище предрассчитанных VTTG-payload ({@code vttg_export}). Снимает основную стоимость
 * полной выгрузки — гидрацию jsonb-сущностей и маппинг на лету: в окне берётся лёгкая проекция
 * (url + время изменения), а полезная нагрузка читается из таблицы.
 *
 * <p>Payload валиден, если совпадают версия логики маппера ({@link #SCHEMA_VERSION}) и время
 * изменения источника. Невалидные/отсутствующие payload пересчитываются на лету и
 * перезаписываются (самозаполнение, отдельный backfill не нужен). Правка сущности меняет её
 * {@code updatedAt}, что автоматически инвалидирует payload — для типов без кросс-сущностных
 * зависимостей (заклинания, существа) этого достаточно, код в сервисах сохранения не требуется.</p>
 *
 * <p>Устойчивость: одна «битая» сущность (не гидрируется из jsonb, падает маппинг) не валит
 * выгрузку типа — пакет пересчёта деградирует до поштучной обработки, сбойная сущность
 * пропускается с ошибкой в логе. Сохранение пересчитанных payload — оптимизация: его сбой
 * (например, гонка параллельных выгрузок) не мешает отдать ответ, собранный в памяти.
 * Поэтому фазы разнесены по собственным транзакциям, а не объединены в одну.</p>
 */
@Service
@RequiredArgsConstructor
public class VttgPayloadStore {

    private static final Logger log = LoggerFactory.getLogger(VttgPayloadStore.class);

    /** Версия логики мапперов. Увеличьте при изменении формата payload — все строки пересчитаются. */
    public static final int SCHEMA_VERSION = 8;
    /**
     * Размер пакета пересчёта/сохранения: ограничивает {@code IN}-список, объём транзакции и
     * зону поражения при сбое (на поштучную обработку переходит только сбойный пакет).
     */
    static final int BATCH_SIZE = 500;

    private final VttgExportRepository repository;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    /**
     * Возвращает изменения одного типа за окно, используя предрассчитанные payload и пересчитывая
     * недостающие. Вызывается параллельно для каждого типа; транзакциями фаз управляет сам.
     *
     * @param type         тип раздела VTTG (например, "spells")
     * @param refsFinder   лёгкая выборка ссылок (url + changedAt) сущностей окна
     * @param dependencyStampFinder отметка зависимостей: максимум времени изменения зависимой
     *                     таблицы (например, базовых предметов для магических). Меняется при правке
     *                     зависимости и инвалидирует payload. {@code () -> null} — зависимостей нет
     * @param byUrlsFinder выборка полных сущностей по набору url (для пересчёта)
     * @param urlOf        извлечение url из сущности
     * @param toDto        маппинг сущности в VTTG-DTO
     */
    public <E> List<VttgChange> load(String type,
                                     Supplier<List<VttgEntityRef>> refsFinder,
                                     Supplier<Instant> dependencyStampFinder,
                                     Function<Collection<String>, List<E>> byUrlsFinder,
                                     Function<E, String> urlOf,
                                     Function<E, Object> toDto) {
        List<VttgEntityRef> refs = inReadOnlyTx(refsFinder::get);
        if (refs.isEmpty()) {
            return List.of();
        }
        Instant dependencyStamp = inReadOnlyTx(dependencyStampFinder::get);

        // Отметка валидности = max(время изменения сущности, отметка зависимостей). Правка как самой
        // сущности, так и её зависимости сдвигает отметку и делает сохранённый payload устаревшим.
        Map<String, Instant> windowStamp = new LinkedHashMap<>();
        Map<String, Instant> validStamp = new LinkedHashMap<>();
        for (VttgEntityRef ref : refs) {
            windowStamp.put(ref.getUrl(), ref.getChangedAt());
            validStamp.put(ref.getUrl(), latest(ref.getChangedAt(), dependencyStamp));
        }

        Map<String, JsonNode> payloads = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (VttgExport stored : inReadOnlyTx(() -> repository.findByTypeAndUrlIn(type, windowStamp.keySet()))) {
            if (isFresh(stored, validStamp.get(stored.getUrl()))) {
                payloads.put(stored.getUrl(), stored.getPayload());
            }
        }
        for (String url : windowStamp.keySet()) {
            if (!payloads.containsKey(url)) {
                missing.add(url);
            }
        }

        if (!missing.isEmpty()) {
            List<VttgExport> recomputed = new ArrayList<>(missing.size());
            for (List<String> batch : batches(missing)) {
                recomputeBatch(type, batch, byUrlsFinder, urlOf, toDto, validStamp, payloads, recomputed);
            }
            persist(type, recomputed);
        }

        List<VttgChange> changes = new ArrayList<>(refs.size());
        for (VttgEntityRef ref : refs) {
            JsonNode payload = payloads.get(ref.getUrl());
            // payload == null — сущность исчезла между выборкой ссылок и пересчётом либо пропущена
            // как «битая» (см. recomputeBatch) — не отдаём.
            if (payload == null) {
                continue;
            }
            // Массив = одна сущность раскрылась в несколько записей (напр. магический предмет с
            // несколькими базами в clarification) — разворачиваем в отдельные изменения по id элемента.
            if (payload.isArray()) {
                for (JsonNode element : payload) {
                    changes.add(new VttgChange(type, elementKey(element, ref.getUrl()), ref.getChangedAt(), element));
                }
            } else {
                changes.add(new VttgChange(type, ref.getUrl(), ref.getChangedAt(), payload));
            }
        }
        return changes;
    }

    /**
     * Пересчитывает пакет url: гидрация + маппинг в одной read-only транзакции (маппер может
     * дочитывать ленивые связи). Если пакет падает целиком (например, сущность не гидрируется
     * из-за некорректного jsonb), деградирует до поштучной обработки: сбойные сущности
     * пропускаются с ошибкой в логе, остальные попадают в выгрузку.
     */
    private <E> void recomputeBatch(String type, List<String> batch,
                                    Function<Collection<String>, List<E>> byUrlsFinder,
                                    Function<E, String> urlOf, Function<E, Object> toDto,
                                    Map<String, Instant> validStamp,
                                    Map<String, JsonNode> payloads, List<VttgExport> recomputed) {
        try {
            inReadOnlyTx(() -> {
                mapEntities(type, byUrlsFinder.apply(batch), urlOf, toDto, validStamp, payloads, recomputed);
                return null;
            });
        } catch (RuntimeException batchFailure) {
            log.warn("VTTG export: пакетный пересчёт {} ({} url) не удался, перехожу на поштучный: {}",
                    type, batch.size(), batchFailure.toString());
            for (String url : batch) {
                try {
                    inReadOnlyTx(() -> {
                        mapEntities(type, byUrlsFinder.apply(List.of(url)), urlOf, toDto, validStamp, payloads, recomputed);
                        return null;
                    });
                } catch (RuntimeException entityFailure) {
                    log.error("VTTG export: сущность {}/{} не выгружается — пропущена в дампе", type, url, entityFailure);
                }
            }
        }
    }

    /** Маппит сущности в payload; сбой одной сущности пропускает её, не прерывая остальные. */
    private <E> void mapEntities(String type, List<E> entities,
                                 Function<E, String> urlOf, Function<E, Object> toDto,
                                 Map<String, Instant> validStamp,
                                 Map<String, JsonNode> payloads, List<VttgExport> recomputed) {
        for (E entity : entities) {
            String url = urlOf.apply(entity);
            try {
                JsonNode payload = objectMapper.valueToTree(toDto.apply(entity));
                payloads.put(url, payload);
                recomputed.add(new VttgExport(type, url, payload, validStamp.get(url), SCHEMA_VERSION));
            } catch (RuntimeException failure) {
                log.error("VTTG export: маппинг {}/{} упал — сущность пропущена в дампе", type, url, failure);
            }
        }
    }

    /**
     * Сохраняет пересчитанные payload пакетами в отдельных транзакциях. Хранилище — кэш:
     * сбой записи (например, гонка параллельных выгрузок по одному url) логируется, но ответ
     * всё равно собирается из уже посчитанных в памяти payload; недостающее пересчитается позже.
     */
    private void persist(String type, List<VttgExport> recomputed) {
        for (List<VttgExport> batch : batches(recomputed)) {
            try {
                inWriteTx(() -> repository.saveAll(batch));
            } catch (RuntimeException failure) {
                log.warn("VTTG export: не удалось сохранить {} payload {} — ответ собран из памяти: {}",
                        batch.size(), type, failure.toString());
            }
        }
    }

    private <T> T inReadOnlyTx(Supplier<T> action) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        return transaction.execute(status -> action.get());
    }

    private void inWriteTx(Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
    }

    private static <T> List<List<T>> batches(List<T> items) {
        List<List<T>> result = new ArrayList<>();
        for (int from = 0; from < items.size(); from += BATCH_SIZE) {
            result.add(items.subList(from, Math.min(items.size(), from + BATCH_SIZE)));
        }
        return result;
    }

    /** Естественный ключ записи из элемента массива: его {@code id}; иначе url исходной сущности. */
    private String elementKey(JsonNode element, String fallbackUrl) {
        JsonNode id = element.get("id");
        return id != null && id.isTextual() ? id.asText() : fallbackUrl;
    }

    private boolean isFresh(VttgExport stored, Instant expectedStamp) {
        return stored.getSchemaVer() == SCHEMA_VERSION
                && expectedStamp != null
                && stored.getSrcUpdatedAt() != null
                && stored.getSrcUpdatedAt().toEpochMilli() == expectedStamp.toEpochMilli();
    }

    private Instant latest(Instant changedAt, Instant dependencyStamp) {
        if (dependencyStamp == null) {
            return changedAt;
        }
        if (changedAt == null || dependencyStamp.isAfter(changedAt)) {
            return dependencyStamp;
        }
        return changedAt;
    }
}
