package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChange;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesResponse;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesStatus;
import club.ttg.dnd5.domain.vttg.repository.VttgEntityRef;
import club.ttg.dnd5.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Сборка инкрементальной дельты сущностей TTG Club для синхронизации с компендиумом VTTG.
 *
 * <p>Окно изменений — полуинтервал {@code (since, until]}. Верхняя граница берётся как
 * {@code now() - SAFETY_LAG}, чтобы не выдать записи параллельных транзакций, которые ещё не
 * закоммичены; такой «хвост» попадёт в следующий запрос (дельта идемпотентна, повтор безопасен).</p>
 *
 * <p>Возвращаются только видимые сущности (добавленные/изменённые). Скрытые сущности
 * ({@code isHiddenEntity = true}) — это механизм мягкого удаления на стороне источника,
 * наружу в VTTG они не отдаются.</p>
 */
@Service
@RequiredArgsConstructor
public class VttgChangesService {

    private static final Logger log = LoggerFactory.getLogger(VttgChangesService.class);

    /** Защитный лаг верхней границы окна против невидимых ещё (незакоммиченных) изменений. */
    static final Duration SAFETY_LAG = Duration.ofMinutes(5);

    private static final String SPELLS = SectionType.SPELL.getValue();
    private static final String BESTIARY = SectionType.BESTIARY.getValue();
    private static final String MAGIC_ITEMS = SectionType.MAGIC_ITEM.getValue();
    private static final String ITEMS = SectionType.ITEM.getValue();
    private static final String BACKGROUNDS = SectionType.BACKGROUND.getValue();
    private static final String FEATS = SectionType.FEAT.getValue();
    private static final String SPECIES = SectionType.SPECIES.getValue();
    private static final Set<String> SUPPORTED_TYPES =
            Set.of(SPELLS, BESTIARY, MAGIC_ITEMS, ITEMS, BACKGROUNDS, FEATS, SPECIES);

    private final SpellRepository spellRepository;
    private final CreatureRepository creatureRepository;
    private final MagicItemRepository magicItemRepository;
    private final ItemRepository itemRepository;
    private final BackgroundRepository backgroundRepository;
    private final FeatRepository featRepository;
    private final SpeciesRepository speciesRepository;
    private final VttgSpellMapper spellMapper;
    private final VttgCreatureMapper creatureMapper;
    private final VttgMagicItemMapper magicItemMapper;
    private final VttgItemMapper itemMapper;
    private final VttgBackgroundMapper backgroundMapper;
    private final VttgFeatMapper featMapper;
    private final VttgSpeciesMapper speciesMapper;
    private final VttgCompendiumSections compendiumSections;
    private final VttgPayloadStore payloadStore;
    private final PlatformTransactionManager transactionManager;
    private final ExecutorService exportExecutor;

    /** Лёгкий статус для индикатора: число изменений в окне без полезной нагрузки. */
    @Transactional(readOnly = true)
    public VttgChangesStatus status(Instant sinceParam, String srdVersion, Set<String> types) {
        Window window = window(sinceParam);
        Set<String> selected = normalizeTypes(types);
        Map<String, Long> byType = new LinkedHashMap<>();

        if (selected.contains(SPELLS)) {
            long count = spellRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(SPELLS, count);
            }
        }
        if (selected.contains(BESTIARY)) {
            long count = creatureRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(BESTIARY, count);
            }
        }
        if (selected.contains(MAGIC_ITEMS)) {
            long count = magicItemRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(MAGIC_ITEMS, count);
            }
        }
        if (selected.contains(ITEMS)) {
            long count = itemRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(ITEMS, count);
            }
        }
        if (selected.contains(BACKGROUNDS)) {
            long count = backgroundRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(BACKGROUNDS, count);
            }
        }
        if (selected.contains(FEATS)) {
            long count = featRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(FEATS, count);
            }
        }
        if (selected.contains(SPECIES)) {
            long count = speciesRepository.countChangedForVttgExport(srdVersion, window.since(), window.until());
            if (count > 0) {
                byType.put(SPECIES, count);
            }
        }

        long total = byType.values().stream().mapToLong(Long::longValue).sum();
        return new VttgChangesStatus(window.since(), window.until(), total > 0, total, byType);
    }

    /**
     * Дельта окна: добавленные/изменённые видимые сущности (upserts) с полезной нагрузкой.
     *
     * <p>Полная выгрузка (без {@code since}) кэшируется ненадолго: она тяжёлая (гидрация jsonb)
     * и детерминирована в пределах окна, повторные первичные загрузки берутся из кэша.
     * Инкрементальный поллинг ({@code since} задан) не кэшируется и всегда свежий; верхняя граница
     * {@code until} в кэшированном ответе «заморожена», что безопасно — повторная выборка идемпотентна.</p>
     */
    @Cacheable(cacheNames = CacheConfig.VTTG_FULL_EXPORT, condition = "#sinceParam == null", key = "{#srdVersion, #types}")
    public VttgChangesResponse changes(Instant sinceParam, String srdVersion, Set<String> types) {
        long startedAt = System.nanoTime();
        Window window = window(sinceParam);
        Set<String> selected = normalizeTypes(types);
        // Общий на ответ кэш разрешения базовых предметов: зачарования «+1/+2/+3» делят одну базу,
        // поэтому повторные сканы таблицы item не выполняются (используется только при пересчёте magic-items).
        Map<String, List<Item>> baseCache = new HashMap<>();

        // Все разделы с данными собираются единообразно из предрассчитанных payload (vttg_export):
        // в окне берётся лёгкая проекция (url + время), payload читается из таблицы, недостающее
        // пересчитывается на лету и сохраняется. Типы с зависимостями передают «отметку зависимостей»
        // (max времени изменения зависимой таблицы), которая инвалидирует payload при правке зависимости.
        Map<String, CompletableFuture<TypeResult>> futures = new LinkedHashMap<>();
        submitStore(futures, SPELLS, selected,
                () -> spellRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                () -> null,
                spellRepository::findAllForVttgExportByUrls, Spell::getUrl, spellMapper::toVttg);
        submitStore(futures, BESTIARY, selected,
                () -> creatureRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                () -> null,
                creatureRepository::findAllForVttgExportByUrls, Creature::getUrl, creatureMapper::toVttg);
        submitStore(futures, ITEMS, selected,
                () -> itemRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                () -> null,
                itemRepository::findAllForVttgExportByUrls, Item::getUrl, itemMapper::toVttg);
        submitStore(futures, MAGIC_ITEMS, selected,
                () -> magicItemRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                itemRepository::maxChangedAtForVttgExport,
                magicItemRepository::findAllForVttgExportByUrls, MagicItem::getUrl,
                item -> magicItemMapper.toVttg(item, baseCache));
        submitStore(futures, BACKGROUNDS, selected,
                () -> backgroundRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                featRepository::maxChangedAtForVttgExport,
                backgroundRepository::findAllForVttgExportByUrls, Background::getUrl, backgroundMapper::toVttg);
        submitStore(futures, SPECIES, selected,
                () -> speciesRepository.findChangedRefsForVttgExport(srdVersion, window.since(), window.until()),
                speciesRepository::maxChangedAtForVttgExport,
                speciesRepository::findAllForVttgExportByUrls, Species::getUrl, speciesMapper::toVttg);

        // Черты выбираются параллельно, но в дельту идут единым блоком ПОСЛЕ сортировки остальных:
        // разделитель категории + её черты в порядке эталона, иначе маркеры «разъедутся» при сортировке.
        CompletableFuture<TypeResult> featsFuture = !selected.contains(FEATS) ? null
                : supplyAsync(FEATS, () -> {
                    long fetchStart = System.nanoTime();
                    List<Feat> feats = featRepository.findChangedForVttgExport(srdVersion, window.since(), window.until());
                    long mapStart = System.nanoTime();
                    List<VttgChange> block = new ArrayList<>();
                    appendFeatChanges(block, feats);
                    return new TypeResult(FEATS, block,
                            millisSince(fetchStart, mapStart), millisSince(mapStart, System.nanoTime()), feats.size());
                });

        Map<String, long[]> timings = new LinkedHashMap<>();
        List<VttgChange> upserts = new ArrayList<>();
        for (CompletableFuture<TypeResult> future : futures.values()) {
            TypeResult result = future.join();
            upserts.addAll(result.changes());
            timings.put(result.type(), new long[]{result.fetchMs(), result.mapMs(), result.count()});
        }
        upserts.sort(Comparator.comparing(VttgChange::updatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));

        if (featsFuture != null) {
            TypeResult result = featsFuture.join();
            upserts.addAll(result.changes());
            timings.put(result.type(), new long[]{result.fetchMs(), result.mapMs(), result.count()});
        }

        VttgChangesResponse response = new VttgChangesResponse(window.until(), upserts, compendiumSections.changesTree());
        logTimings(timings, upserts.size(), millisSince(startedAt, System.nanoTime()));
        return response;
    }

    /**
     * Планирует параллельную сборку типа из предрассчитанных payload ({@link VttgPayloadStore}).
     * Транзакцией управляет store (она записываемая — пересчитанные payload сохраняются).
     */
    private <E> void submitStore(Map<String, CompletableFuture<TypeResult>> futures, String type, Set<String> selected,
                                 Supplier<List<VttgEntityRef>> refsFinder,
                                 Supplier<Instant> dependencyStampFinder,
                                 Function<Collection<String>, List<E>> byUrlsFinder,
                                 Function<E, String> urlOf, Function<E, Object> toDto) {
        if (!selected.contains(type)) {
            return;
        }
        futures.put(type, CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            List<VttgChange> changes =
                    payloadStore.load(type, refsFinder, dependencyStampFinder, byUrlsFinder, urlOf, toDto);
            return new TypeResult(type, changes, millisSince(start, System.nanoTime()), 0, changes.size());
        }, exportExecutor));
    }

    /** Запускает задачу в пуле экспорта, оборачивая её в отдельную read-only транзакцию. */
    private CompletableFuture<TypeResult> supplyAsync(String type, Supplier<TypeResult> task) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return CompletableFuture.supplyAsync(() -> transaction.execute(status -> task.get()), exportExecutor);
    }

    /** Результат обработки одного типа: готовые upserts и тайминги фаз. */
    private record TypeResult(String type, List<VttgChange> changes, long fetchMs, long mapMs, int count) {
    }

    /** Логирует разбивку по фазам (fetch/map по типам); сериализация JSON выполняется вне этого метода. */
    private void logTimings(Map<String, long[]> timings, int totalUpserts, long totalMs) {
        if (!log.isInfoEnabled()) {
            return;
        }
        StringBuilder breakdown = new StringBuilder();
        timings.forEach((type, t) -> breakdown.append(' ').append(type)
                .append("=[fetch ").append(t[0]).append("ms, map ").append(t[1]).append("ms, n=").append(t[2]).append(']'));
        log.info("VTTG /changes: {} upserts за {}ms (fetch+map+sort, без сериализации);{}",
                totalUpserts, totalMs, breakdown);
    }

    private static long millisSince(long fromNanos, long toNanos) {
        return (toNanos - fromNanos) / 1_000_000;
    }

    /**
     * Добавляет черты в дельту единым блоком: для каждой непустой категории — её разделитель
     * ({@code type:"separator"}) и следом черты этой категории. Порядок категорий и подписи
     * разделителей задаёт {@link VttgFeatMapper}; черты внутри категории — по имени.
     * Черты без категории идут в конце без разделителя.
     */
    private void appendFeatChanges(List<VttgChange> target, List<Feat> feats) {
        Map<FeatCategory, List<Feat>> grouped = new LinkedHashMap<>();
        for (FeatCategory category : featMapper.separatorOrder()) {
            grouped.put(category, new ArrayList<>());
        }
        List<Feat> uncategorized = new ArrayList<>();
        for (Feat feat : feats) {
            List<Feat> bucket = feat.getCategory() == null ? uncategorized : grouped.get(feat.getCategory());
            bucket.add(feat);
        }

        Comparator<Feat> byName = Comparator.comparing(Feat::getName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        for (Map.Entry<FeatCategory, List<Feat>> entry : grouped.entrySet()) {
            List<Feat> group = entry.getValue();
            if (group.isEmpty()) {
                continue;
            }
            Map<String, Object> separator = featMapper.separator(entry.getKey());
            target.add(new VttgChange(FEATS, "separator/" + separator.get("id"), null, separator));
            group.sort(byName);
            group.forEach(feat -> target.add(featChange(feat)));
        }
        uncategorized.sort(byName);
        uncategorized.forEach(feat -> target.add(featChange(feat)));
    }

    private VttgChange featChange(Feat feat) {
        return new VttgChange(FEATS, feat.getUrl(),
                changedAt(feat.getUpdatedAt(), feat.getCreatedAt()), featMapper.toVttg(feat));
    }

    private Window window(Instant sinceParam) {
        Instant since = sinceParam == null ? Instant.EPOCH : sinceParam;
        Instant until = Instant.now().minus(SAFETY_LAG);
        // Если курсор «в будущем» относительно среза — окно пустое (until == since).
        if (until.isBefore(since)) {
            until = since;
        }
        return new Window(since, until);
    }

    private Set<String> normalizeTypes(Set<String> types) {
        if (types == null || types.isEmpty()) {
            return SUPPORTED_TYPES;
        }
        Set<String> normalized = types.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(SUPPORTED_TYPES::contains)
                .collect(Collectors.toSet());
        return normalized.isEmpty() ? SUPPORTED_TYPES : normalized;
    }

    private static Instant changedAt(Instant updatedAt, Instant createdAt) {
        return updatedAt != null ? updatedAt : createdAt;
    }

    private record Window(Instant since, Instant until) {
    }
}
