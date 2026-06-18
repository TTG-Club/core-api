package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.model.VttgExport;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import club.ttg.dnd5.domain.vttg.repository.VttgExportRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 */
@Service
@RequiredArgsConstructor
public class VttgPayloadStore {

    /** Версия логики мапперов. Увеличьте при изменении формата payload — все строки пересчитаются. */
    public static final int SCHEMA_VERSION = 1;

    private final VttgExportRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Возвращает изменения одного типа за окно, используя предрассчитанные payload и пересчитывая
     * недостающие. Выполняется в собственной (записываемой) транзакции — вызывается параллельно
     * для каждого типа.
     *
     * @param type        тип раздела VTTG (например, "spells")
     * @param refsFinder  лёгкая выборка ссылок (url + changedAt) сущностей окна
     * @param byUrlsFinder выборка полных сущностей по набору url (для пересчёта)
     * @param urlOf       извлечение url из сущности
     * @param toDto       маппинг сущности в VTTG-DTO
     */
    @Transactional
    public <E> List<VttgChange> load(String type,
                                     Supplier<List<VttgEntityRef>> refsFinder,
                                     Function<Collection<String>, List<E>> byUrlsFinder,
                                     Function<E, String> urlOf,
                                     Function<E, Object> toDto) {
        List<VttgEntityRef> refs = refsFinder.get();
        if (refs.isEmpty()) {
            return List.of();
        }

        Map<String, Instant> changedAt = new LinkedHashMap<>();
        for (VttgEntityRef ref : refs) {
            changedAt.put(ref.getUrl(), ref.getChangedAt());
        }

        Map<String, JsonNode> payloads = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        for (VttgExport stored : repository.findByTypeAndUrlIn(type, changedAt.keySet())) {
            if (isFresh(stored, changedAt.get(stored.getUrl()))) {
                payloads.put(stored.getUrl(), stored.getPayload());
            }
        }
        for (String url : changedAt.keySet()) {
            if (!payloads.containsKey(url)) {
                missing.add(url);
            }
        }

        if (!missing.isEmpty()) {
            List<VttgExport> recomputed = new ArrayList<>(missing.size());
            for (E entity : byUrlsFinder.apply(missing)) {
                String url = urlOf.apply(entity);
                JsonNode payload = objectMapper.valueToTree(toDto.apply(entity));
                payloads.put(url, payload);
                recomputed.add(new VttgExport(type, url, payload, changedAt.get(url), SCHEMA_VERSION));
            }
            repository.saveAll(recomputed);
        }

        List<VttgChange> changes = new ArrayList<>(refs.size());
        for (VttgEntityRef ref : refs) {
            JsonNode payload = payloads.get(ref.getUrl());
            // payload == null лишь если сущность исчезла между выборкой ссылок и пересчётом — пропускаем.
            if (payload != null) {
                changes.add(new VttgChange(type, ref.getUrl(), ref.getChangedAt(), payload));
            }
        }
        return changes;
    }

    private boolean isFresh(VttgExport stored, Instant expectedChangedAt) {
        return stored.getSchemaVer() == SCHEMA_VERSION
                && expectedChangedAt != null
                && stored.getSrcUpdatedAt() != null
                && stored.getSrcUpdatedAt().toEpochMilli() == expectedChangedAt.toEpochMilli();
    }
}
