package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.SourceGroupMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Сервис метаданных фильтров видов.
 * Строит {@link FilterMetadataResponse} напрямую, без legacy FilterGroup.
 */
@Service
@RequiredArgsConstructor
public class SpeciesFilterService
{
    private final SpeciesRepository speciesRepository;
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>(2);

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(SpeciesQueryRequest.class, "creatureType"))
                .name("Тип существа")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(Arrays.stream(CreatureType.values())
                        .map(ct -> FilterValueMeta.builder()
                                .id(ct.name())
                                .value(ct.name())
                                .name(ct.getName())
                                .build())
                        .toList())
                .build());

        // Версия SRD
        List<String> srdVersions = speciesRepository.findDistinctSrdVersions();
        if (!srdVersions.isEmpty()) {
            groups.add(FilterGroupMeta.builder()
                    .key(FilterKeys.keyOf(SpeciesQueryRequest.class, "srdVersion"))
                    .name("Версия SRD")
                    .supports(SupportsConfig.builder().mode(true).union(false).build())
                    .values(srdVersions.stream()
                            .map(v -> FilterValueMeta.builder()
                                    .id(v)
                                    .value(v)
                                    .name("SRD " + v)
                                    .build())
                            .toList())
                    .build());
        }

        return groups;
    }

    private List<SourceGroupMeta> buildSourceGroups(Set<String> selectedSources)
    {
        List<String> usedSourceCodes = speciesRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}
