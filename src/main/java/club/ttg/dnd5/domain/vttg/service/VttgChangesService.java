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
import club.ttg.dnd5.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    @Transactional(readOnly = true)
    public VttgChangesResponse changes(Instant sinceParam, String srdVersion, Set<String> types) {
        long startedAt = System.nanoTime();
        Window window = window(sinceParam);
        Set<String> selected = normalizeTypes(types);
        List<VttgChange> upserts = new ArrayList<>();
        Map<String, long[]> timings = new LinkedHashMap<>();
        // Общий на ответ кэш разрешения базовых предметов: зачарования «+1/+2/+3» делят одну базу,
        // поэтому повторные сканы таблицы item не выполняются.
        Map<String, List<Item>> baseCache = new HashMap<>();

        collect(upserts, SPELLS, selected, timings,
                () -> spellRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                spell -> new VttgChange(SPELLS, spell.getUrl(),
                        changedAt(spell.getUpdatedAt(), spell.getCreatedAt()), spellMapper.toVttg(spell)));
        collect(upserts, BESTIARY, selected, timings,
                () -> creatureRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                creature -> new VttgChange(BESTIARY, creature.getUrl(),
                        changedAt(creature.getUpdatedAt(), creature.getCreatedAt()), creatureMapper.toVttg(creature)));
        collect(upserts, MAGIC_ITEMS, selected, timings,
                () -> magicItemRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                item -> new VttgChange(MAGIC_ITEMS, item.getUrl(),
                        changedAt(item.getUpdatedAt(), item.getCreatedAt()), magicItemMapper.toVttg(item, baseCache)));
        collect(upserts, ITEMS, selected, timings,
                () -> itemRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                item -> new VttgChange(ITEMS, item.getUrl(),
                        changedAt(item.getUpdatedAt(), item.getCreatedAt()), itemMapper.toVttg(item)));
        collect(upserts, BACKGROUNDS, selected, timings,
                () -> backgroundRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                background -> new VttgChange(BACKGROUNDS, background.getUrl(),
                        changedAt(background.getUpdatedAt(), background.getCreatedAt()), backgroundMapper.toVttg(background)));
        collect(upserts, SPECIES, selected, timings,
                () -> speciesRepository.findChangedForVttgExport(srdVersion, window.since(), window.until()),
                species -> new VttgChange(SPECIES, species.getUrl(),
                        changedAt(species.getUpdatedAt(), species.getCreatedAt()), speciesMapper.toVttg(species)));

        upserts.sort(Comparator.comparing(VttgChange::updatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Черты идут единым блоком в конце: разделитель категории + её черты (порядок эталона),
        // чтобы маркеры-разделители не «разъезжались» при сортировке остальных сущностей по времени.
        if (selected.contains(FEATS)) {
            long fetchStart = System.nanoTime();
            List<Feat> feats = featRepository.findChangedForVttgExport(srdVersion, window.since(), window.until());
            long mapStart = System.nanoTime();
            appendFeatChanges(upserts, feats);
            timings.put(FEATS, new long[]{
                    millisSince(fetchStart, mapStart), millisSince(mapStart, System.nanoTime()), feats.size()});
        }

        VttgChangesResponse response = new VttgChangesResponse(window.until(), upserts, compendiumSections.changesTree());
        logTimings(timings, upserts.size(), millisSince(startedAt, System.nanoTime()));
        return response;
    }

    /**
     * Выбирает изменённые сущности одного типа и добавляет их в дельту, замеряя время выборки из БД
     * и время маппинга (для диагностики узких мест полной выгрузки {@code since=EPOCH}).
     */
    private <E> void collect(List<VttgChange> target, String type, Set<String> selected,
                             Map<String, long[]> timings,
                             Supplier<List<E>> finder, Function<E, VttgChange> mapper) {
        if (!selected.contains(type)) {
            return;
        }
        long fetchStart = System.nanoTime();
        List<E> entities = finder.get();
        long mapStart = System.nanoTime();
        for (E entity : entities) {
            target.add(mapper.apply(entity));
        }
        timings.put(type, new long[]{
                millisSince(fetchStart, mapStart), millisSince(mapStart, System.nanoTime()), entities.size()});
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
