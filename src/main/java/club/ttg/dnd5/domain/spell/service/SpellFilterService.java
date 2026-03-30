package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.SourceGroupMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.LongStream;

/**
 * Сервис метаданных фильтров заклинаний.
 * Строит {@link FilterMetadataResponse} напрямую, без legacy FilterGroup.
 */
@Service
@RequiredArgsConstructor
public class SpellFilterService {
        private final ClassService classService;
        private final SpellRepository spellRepository;
        private final SourceSavedFilterService sourceSavedFilterService;

        /**
         * Предопределённые значения для фильтра «Время накладывания».
         */
        private record CastingTimeOption(Long value, CastingUnit unit) {
        }

        private static final List<CastingTimeOption> CASTING_TIME_OPTIONS = List.of(
                        new CastingTimeOption(null, CastingUnit.BONUS),
                        new CastingTimeOption(null, CastingUnit.REACTION),
                        new CastingTimeOption(null, CastingUnit.ACTION),
                        new CastingTimeOption(1L, CastingUnit.MINUTE),
                        new CastingTimeOption(10L, CastingUnit.MINUTE),
                        new CastingTimeOption(1L, CastingUnit.HOUR),
                        new CastingTimeOption(8L, CastingUnit.HOUR),
                        new CastingTimeOption(12L, CastingUnit.HOUR),
                        new CastingTimeOption(24L, CastingUnit.HOUR));

        /**
         * Предопределённые значения для фильтра «Длительность».
         */
        private record DurationOption(Long value, DurationUnit unit) {
        }

        private static final List<DurationOption> DURATION_OPTIONS = List.of(
                        new DurationOption(null, DurationUnit.INSTANT),
                        new DurationOption(null, DurationUnit.ROUND),
                        new DurationOption(1L, DurationUnit.MINUTE),
                        new DurationOption(10L, DurationUnit.MINUTE),
                        new DurationOption(1L, DurationUnit.HOUR),
                        new DurationOption(8L, DurationUnit.HOUR),
                        new DurationOption(12L, DurationUnit.HOUR),
                        new DurationOption(24L, DurationUnit.HOUR),
                        new DurationOption(1L, DurationUnit.DAY),
                        new DurationOption(7L, DurationUnit.DAY),
                        new DurationOption(10L, DurationUnit.DAY),
                        new DurationOption(1L, DurationUnit.YEAR),
                        new DurationOption(null, DurationUnit.UNTIL_DISPEL),
                        new DurationOption(null, DurationUnit.PERMANENT));

        public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources) {
                return FilterMetadataResponse.builder()
                                .filters(buildFilterGroups())
                                .sources(buildSourceGroups(selectedSources))
                                .build();
        }

        private List<FilterGroupMeta> buildFilterGroups() {
                List<FilterGroupMeta> groups = new ArrayList<>();

                // Школа магии
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "school"))
                                .name("Школа")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(Arrays.stream(MagicSchool.values())
                                                .map(s -> FilterValueMeta.builder()
                                                                .id(s.name())
                                                                .value(s.name())
                                                                .name(s.getName())
                                                                .build())
                                                .toList())
                                .build());

                // Уровень
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "level"))
                                .name("Уровень")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(LongStream.range(0, 10)
                                                .mapToObj(i -> FilterValueMeta.builder()
                                                                .id(String.valueOf(i))
                                                                .value(i)
                                                                .name(i == 0 ? "заговор" : String.valueOf(i))
                                                                .build())
                                                .toList())
                                .build());

                // Классы
                List<CharacterClass> magicClasses = classService.findAllMagicClasses();
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "className"))
                                .name("Классы")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(magicClasses.stream()
                                                .filter(Objects::nonNull)
                                                .map(c -> FilterValueMeta.builder()
                                                                .id(c.getUrl())
                                                                .value(c.getUrl())
                                                                .name("%s [%s]".formatted(c.getName(),
                                                                                c.getSource().getAcronym()))
                                                                .build())
                                                .toList())
                                .build());

                // Подклассы
                List<CharacterClass> magicSubclasses = classService.findAllMagicSubclasses();
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "subclassName"))
                                .name("Подклассы")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(magicSubclasses.stream()
                                                .filter(Objects::nonNull)
                                                .sorted(Comparator.comparing(CharacterClass::getName))
                                                .map(c -> FilterValueMeta.builder()
                                                                .id(c.getUrl())
                                                                .value(c.getUrl())
                                                                .name("%s [%s]".formatted(c.getName(),
                                                                                c.getSource().getAcronym()))
                                                                .build())
                                                .toList())
                                .build());

                // Тип урона
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "damageType"))
                                .name("Тип урона")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(DamageType.values())
                                                .map(d -> FilterValueMeta.builder()
                                                                .id(d.name())
                                                                .value(d.name())
                                                                .name(d.getName())
                                                                .build())
                                                .toList())
                                .build());

                // Тип лечения
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "healingType"))
                                .name("Тип лечения")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(HealingType.values())
                                                .map(h -> FilterValueMeta.builder()
                                                                .id(h.name())
                                                                .value(h.name())
                                                                .name(h.getName())
                                                                .build())
                                                .toList())
                                .build());

                // Накладываемые состояния
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "condition"))
                                .name("Накладываемые состояния")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(Condition.values())
                                                .map(c -> FilterValueMeta.builder()
                                                                .id(c.name())
                                                                .value(c.name())
                                                                .name(c.getName())
                                                                .build())
                                                .toList())
                                .build());

                // Спасброски
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "savingThrow"))
                                .name("Спасброски")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(Ability.values())
                                                .map(a -> FilterValueMeta.builder()
                                                                .id(a.name())
                                                                .value(a.name())
                                                                .name(a.getName())
                                                                .build())
                                                .toList())
                                .build());

                // Время накладывания
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "castingTime"))
                                .name("Время накладывания")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(buildCastingTimeValues())
                                .build());

                // Длительность
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "duration"))
                                .name("Длительность")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(buildDurationValues())
                                .build());

                // Ритуал
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "ritual"))
                                .name("Ритуал")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(List.of(FilterValueMeta.builder()
                                                .id("1")
                                                .value("1")
                                                .name("Требуется")
                                                .build()))
                                .build());

                // Концентрация
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "concentration"))
                                .name("Концентрация")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(List.of(FilterValueMeta.builder()
                                                .id("1")
                                                .value("1")
                                                .name("Требуется")
                                                .build()))
                                .build());

                // Улучшается с уровнем ячейки
                groups.add(FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(SpellQueryRequest.class, "upcastable"))
                                .name("Улучшается с уровнем ячейки")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(List.of(FilterValueMeta.builder()
                                                .id("1")
                                                .value("1")
                                                .name("Требуется")
                                                .build()))
                                .build());

                return groups;
        }

        private List<FilterValueMeta> buildCastingTimeValues() {
                return CASTING_TIME_OPTIONS.stream()
                                .map(opt -> {
                                        String id = opt.value() == null
                                                        ? opt.unit().name()
                                                        : opt.value() + "_" + opt.unit().name();
                                        String name = SpellCastingTime.of(opt.value(), opt.unit()).toString();
                                        return FilterValueMeta.builder()
                                                        .id(id)
                                                        .value(id)
                                                        .name(name)
                                                        .build();
                                })
                                .toList();
        }

        private List<FilterValueMeta> buildDurationValues() {
                return DURATION_OPTIONS.stream()
                                .map(opt -> {
                                        String id = opt.value() == null
                                                        ? opt.unit().name()
                                                        : opt.value() + "_" + opt.unit().name();
                                        String name = SpellDuration.of(opt.value(), opt.unit()).toString();
                                        return FilterValueMeta.builder()
                                                        .id(id)
                                                        .value(id)
                                                        .name(name)
                                                        .build();
                                })
                                .toList();
        }

        private List<SourceGroupMeta> buildSourceGroups(Set<String> selectedSources) {
                List<String> usedSourceCodes = spellRepository.findAllUsedSourceCodes();
                var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

                return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
        }
}