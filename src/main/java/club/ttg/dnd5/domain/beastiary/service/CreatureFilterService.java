package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.filter.model.FilterHashMapping;
import club.ttg.dnd5.domain.filter.repository.FilterHashMappingRepository;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.SourceGroupMeta;
import club.ttg.dnd5.domain.filter.model.FilterHashCategory;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.dto.base.filters.FilterIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис метаданных фильтров бестиария.
 * Строит {@link FilterMetadataResponse} напрямую, без legacy FilterGroup.
 */
@Service
@RequiredArgsConstructor
public class CreatureFilterService
{
    private final CreatureRepository creatureRepository;
    private final SourceSavedFilterService sourceSavedFilterService;
    private final FilterHashMappingRepository filterHashMappingRepository;

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

        // CR
        groups.add(FilterGroupMeta.builder()
                .key("cr")
                .name("Уровень опасности")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(ChallengeRating.values())
                        .map(cr -> FilterValueMeta.builder()
                                .id(String.valueOf(cr.getExperience()))
                                .value(cr.getExperience())
                                .name(cr.getName())
                                .build())
                        .toList())
                .build());

        // Тип существа
        groups.add(FilterGroupMeta.builder()
                .key("type")
                .name("Тип")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(CreatureType.values())
                        .sorted(Comparator.comparing(CreatureType::getName))
                        .map(ct -> FilterValueMeta.builder()
                                .id(ct.name())
                                .value(ct.name())
                                .name(ct.getName())
                                .build())
                        .toList())
                .build());

        // Размер
        groups.add(FilterGroupMeta.builder()
                .key("size")
                .name("Размер")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(Size.values())
                        .map(s -> FilterValueMeta.builder()
                                .id(s.name())
                                .value(s.name())
                                .name(s.getName())
                                .build())
                        .toList())
                .build());

        // Мировоззрение
        groups.add(FilterGroupMeta.builder()
                .key("alignment")
                .name("Мировоззрение")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(Alignment.values())
                        .map(a -> FilterValueMeta.builder()
                                .id(a.name())
                                .value(a.name())
                                .name(a.getName())
                                .build())
                        .toList())
                .build());

        // Место обитания
        groups.add(FilterGroupMeta.builder()
                .key("habitat")
                .name("Место обитания")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(Habitat.values())
                        .map(h -> FilterValueMeta.builder()
                                .id(h.name())
                                .value(h.name())
                                .name(h.getName())
                                .build())
                        .toList())
                .build());

        // Чувства
        groups.add(FilterGroupMeta.builder()
                .key("senses")
                .name("Чувства")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(Arrays.stream(CreatureSenses.values())
                        .map(s -> FilterValueMeta.builder()
                                .id(s.name())
                                .value(s.name())
                                .name(s.getName())
                                .build())
                        .toList())
                .build());

        // Теги — из DISTINCT-запроса + pre-persisted хэши
        Map<String, String> tagHashes = filterHashMappingRepository.findAllByCategory(FilterHashCategory.TAG)
                .stream()
                .collect(Collectors.toMap(FilterHashMapping::getValue, FilterHashMapping::getHash));

        List<String> tags = creatureRepository.findDistinctTags();

        groups.add(FilterGroupMeta.builder()
                .key("tag")
                .name("Тег типа")
                .type("filter")
                .supportsMode(true)
                .supportsUnion(true)
                .values(tags.stream()
                        .map(tag -> {
                            String hash = tagHashes.getOrDefault(tag, FilterIdUtils.shortHash(tag));
                            return FilterValueMeta.builder()
                                    .id(hash)
                                    .value(tag)
                                    .name(tag)
                                    .build();
                        })
                        .toList())
                .build());

        // Логово (singleton)
        groups.add(FilterGroupMeta.builder()
                .key("lair")
                .name("Логово")
                .type("singleton")
                .supportsMode(false)
                .supportsUnion(false)
                .build());

        // Легендарное действие (singleton)
        groups.add(FilterGroupMeta.builder()
                .key("legendaryAction")
                .name("Легендарное действие")
                .type("singleton")
                .supportsMode(false)
                .supportsUnion(false)
                .build());

        return groups;
    }

    private List<SourceGroupMeta> buildSourceGroups()
    {
        List<String> usedSourceCodes = creatureRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}