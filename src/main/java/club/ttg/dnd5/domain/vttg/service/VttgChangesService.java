package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChange;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesResponse;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

    /** Защитный лаг верхней границы окна против невидимых ещё (незакоммиченных) изменений. */
    static final Duration SAFETY_LAG = Duration.ofMinutes(5);

    private static final String SPELLS = SectionType.SPELL.getValue();
    private static final String BESTIARY = SectionType.BESTIARY.getValue();
    private static final String MAGIC_ITEMS = SectionType.MAGIC_ITEM.getValue();
    private static final String ITEMS = SectionType.ITEM.getValue();
    private static final String BACKGROUNDS = SectionType.BACKGROUND.getValue();
    private static final Set<String> SUPPORTED_TYPES = Set.of(SPELLS, BESTIARY, MAGIC_ITEMS, ITEMS, BACKGROUNDS);

    private final SpellRepository spellRepository;
    private final CreatureRepository creatureRepository;
    private final MagicItemRepository magicItemRepository;
    private final ItemRepository itemRepository;
    private final BackgroundRepository backgroundRepository;
    private final VttgSpellMapper spellMapper;
    private final VttgCreatureMapper creatureMapper;
    private final VttgMagicItemMapper magicItemMapper;
    private final VttgItemMapper itemMapper;
    private final VttgBackgroundMapper backgroundMapper;
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

        long total = byType.values().stream().mapToLong(Long::longValue).sum();
        return new VttgChangesStatus(window.since(), window.until(), total > 0, total, byType);
    }

    /** Дельта окна: добавленные/изменённые видимые сущности (upserts) с полезной нагрузкой. */
    @Transactional(readOnly = true)
    public VttgChangesResponse changes(Instant sinceParam, String srdVersion, Set<String> types) {
        Window window = window(sinceParam);
        Set<String> selected = normalizeTypes(types);
        List<VttgChange> upserts = new ArrayList<>();

        if (selected.contains(SPELLS)) {
            for (Spell spell : spellRepository.findChangedForVttgExport(srdVersion, window.since(), window.until())) {
                upserts.add(new VttgChange(SPELLS, spell.getUrl(),
                        changedAt(spell.getUpdatedAt(), spell.getCreatedAt()), spellMapper.toVttg(spell)));
            }
        }
        if (selected.contains(BESTIARY)) {
            for (Creature creature : creatureRepository.findChangedForVttgExport(srdVersion, window.since(), window.until())) {
                upserts.add(new VttgChange(BESTIARY, creature.getUrl(),
                        changedAt(creature.getUpdatedAt(), creature.getCreatedAt()), creatureMapper.toVttg(creature)));
            }
        }
        if (selected.contains(MAGIC_ITEMS)) {
            for (MagicItem item : magicItemRepository.findChangedForVttgExport(srdVersion, window.since(), window.until())) {
                upserts.add(new VttgChange(MAGIC_ITEMS, item.getUrl(),
                        changedAt(item.getUpdatedAt(), item.getCreatedAt()), magicItemMapper.toVttg(item)));
            }
        }
        if (selected.contains(ITEMS)) {
            for (Item item : itemRepository.findChangedForVttgExport(srdVersion, window.since(), window.until())) {
                upserts.add(new VttgChange(ITEMS, item.getUrl(),
                        changedAt(item.getUpdatedAt(), item.getCreatedAt()), itemMapper.toVttg(item)));
            }
        }
        if (selected.contains(BACKGROUNDS)) {
            for (Background background : backgroundRepository.findChangedForVttgExport(srdVersion, window.since(), window.until())) {
                upserts.add(new VttgChange(BACKGROUNDS, background.getUrl(),
                        changedAt(background.getUpdatedAt(), background.getCreatedAt()), backgroundMapper.toVttg(background)));
            }
        }

        upserts.sort(Comparator.comparing(VttgChange::updatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return new VttgChangesResponse(window.until(), upserts, compendiumSections.changesTree());
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
