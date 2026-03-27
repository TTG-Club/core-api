package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.SourceGroupMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.dto.base.filters.FilterIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;

/**
 * Сервис метаданных фильтров заклинаний.
 * Строит {@link FilterMetadataResponse} напрямую, без legacy FilterGroup.
 */
@Service
@RequiredArgsConstructor
public class SpellFilterService
{
    private final ClassService classService;
    private final SpellRepository spellRepository;
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata()
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups())
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>();

        // Школа магии
        groups.add(FilterGroupMeta.builder()
                .key("school")
                .name("Школа")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
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
                .key("level")
                .name("Уровень")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
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
                .key("className")
                .name("Классы")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(magicClasses.stream()
                        .filter(Objects::nonNull)
                        .map(c -> FilterValueMeta.builder()
                                .id(FilterIdUtils.shortHash(c.getUrl()))
                                .value(c.getUrl())
                                .name("%s [%s]".formatted(c.getName(), c.getSource().getAcronym()))
                                .build())
                        .toList())
                .build());

        // Подклассы
        List<CharacterClass> magicSubclasses = classService.findAllMagicSubclasses();
        groups.add(FilterGroupMeta.builder()
                .key("subclassName")
                .name("Подклассы")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(magicSubclasses.stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(CharacterClass::getName))
                        .map(c -> FilterValueMeta.builder()
                                .id(FilterIdUtils.shortHash(c.getUrl()))
                                .value(c.getUrl())
                                .name("%s [%s]".formatted(c.getName(), c.getSource().getAcronym()))
                                .build())
                        .toList())
                .build());

        // Тип урона
        groups.add(FilterGroupMeta.builder()
                .key("damageType")
                .name("Тип урона")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
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
                .key("healingType")
                .name("Тип лечения")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
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
                .key("condition")
                .name("Накладываемые состояния")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
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
                .key("savingThrow")
                .name("Спасброски")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(Ability.values())
                        .map(a -> FilterValueMeta.builder()
                                .id(a.name())
                                .value(a.name())
                                .name(a.getName())
                                .build())
                        .toList())
                .build());

        // Ритуал (singleton)
        groups.add(FilterGroupMeta.builder()
                .key("ritual")
                .name("Ритуал")
                .type("singleton")
                .supportsMode(false)
                .supportsUnion(false)
                .build());

        // Концентрация (singleton)
        groups.add(FilterGroupMeta.builder()
                .key("concentration")
                .name("Концентрация")
                .type("singleton")
                .supportsMode(false)
                .supportsUnion(false)
                .build());

        // Улучшается с уровнем ячейки (singleton)
        groups.add(FilterGroupMeta.builder()
                .key("upcastable")
                .name("Улучшается с уровнем ячейки")
                .type("singleton")
                .supportsMode(false)
                .supportsUnion(false)
                .build());

        return groups;
    }

    private List<SourceGroupMeta> buildSourceGroups()
    {
        List<String> usedSourceCodes = spellRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}