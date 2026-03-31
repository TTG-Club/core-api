package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
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
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
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
import java.util.Set;
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

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>();

        // CR
        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "cr"))
                .name("Уровень опасности")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "type"))
                .name("Тип")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "size"))
                .name("Размер")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "alignment"))
                .name("Мировоззрение")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "habitat"))
                .name("Место обитания")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "senses"))
                .name("Чувства")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
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
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "tag"))
                .name("Тег типа")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
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

        // Логово
        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "lair"))
                .name("Логово")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(List.of(FilterValueMeta.builder()
                        .id("1")
                        .value("1")
                        .name("Есть")
                        .build()))
                .build());

        // Легендарное действие
        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(CreatureQueryRequest.class, "legendaryAction"))
                .name("Легендарное действие")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(List.of(FilterValueMeta.builder()
                        .id("1")
                        .value("1")
                        .name("Есть")
                        .build()))
                .build());

        return groups;
    }

    private List<SourceGroupMeta> buildSourceGroups(Set<String> selectedSources)
    {
        List<String> usedSourceCodes = creatureRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}