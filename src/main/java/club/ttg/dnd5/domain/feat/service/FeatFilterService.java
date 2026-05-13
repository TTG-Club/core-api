package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.feat.rest.dto.FeatQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeatFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;
    private final FeatRepository featRepository;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .filters(buildFilterGroups())
                .sources(buildSourceGroups(selectedSources))
                .build();
    }

    private List<FilterGroupMeta> buildFilterGroups()
    {
        List<FilterGroupMeta> groups = new ArrayList<>(4);

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(FeatQueryRequest.class, "category"))
                .name("Категория")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(Arrays.stream(FeatCategory.values())
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getName())
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(FeatQueryRequest.class, "ability"))
                .name("Характеристика")
                .supports(SupportsConfig.builder().mode(true).union(true).build())
                .values(Arrays.stream(Ability.values())
                        .map(v -> FilterValueMeta.builder()
                                .id(v.name())
                                .value(v.name())
                                .name(v.getShortName())
                                .build())
                        .toList())
                .build());

        groups.add(FilterGroupMeta.builder()
                .key(FilterKeys.keyOf(FeatQueryRequest.class, "repeatability"))
                .name("Повторяемость")
                .supports(SupportsConfig.builder().mode(true).union(false).build())
                .values(List.of(FilterValueMeta.builder()
                        .id("1")
                        .value("1")
                        .name("Да")
                        .build()))
                .build());

        // Версия SRD
        List<String> srdVersions = featRepository.findDistinctSrdVersions();
        if (!srdVersions.isEmpty()) {
            groups.add(FilterGroupMeta.builder()
                    .key(FilterKeys.keyOf(FeatQueryRequest.class, "srdVersion"))
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

    private List<FilterMetadataResponse.SourceGroupMeta> buildSourceGroups(Set<String> selectedSources)
    {
        List<String> usedSourceCodes = featRepository.findAllUsedSourceCodes();
        var legacySources = sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes, selectedSources);

        return FilterMetadataMapper.mapSourcesFromFilterInfo(legacySources);
    }
}
