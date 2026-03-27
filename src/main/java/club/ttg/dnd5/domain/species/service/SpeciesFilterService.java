package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.SourceGroupMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

    public FilterMetadataResponse getFilterMetadata()
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups())
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        return List.of(
                FilterGroupMeta.builder()
                        .key("creatureType")
                        .name("Тип существа")
                        .type("filter")
                        .supportsMode(true)
                        .supportsUnion(true)
                        .values(Arrays.stream(CreatureType.values())
                                .map(ct -> FilterValueMeta.builder()
                                        .id(ct.name())
                                        .value(ct.name())
                                        .name(ct.getName())
                                        .build())
                                .toList())
                        .build()
        );
    }

    private List<SourceGroupMeta> buildSourceGroups()
    {
        List<String> usedSourceCodes = speciesRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}