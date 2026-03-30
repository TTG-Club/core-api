package club.ttg.dnd5.domain.feat.service;

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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeatFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo(selectedSources)))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(FeatQueryRequest.class, "category"))
                                .name("Категория")
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(FeatCategory.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
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
                                .build(),
                        FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(FeatQueryRequest.class, "repeatability"))
                                .name("Повторяемость")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(List.of(FilterValueMeta.builder()
                                        .id("1")
                                        .value("1")
                                        .name("Да")
                                        .build()))
                                .build()
                ))
                .build();
    }
}
