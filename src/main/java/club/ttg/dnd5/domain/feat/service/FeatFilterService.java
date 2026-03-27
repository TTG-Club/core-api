package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata()
    {
        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo()))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key("category")
                                .name("Категория")
                                .type("filter")
                                .supportsMode(true)
                                .supportsUnion(true)
                                .values(Arrays.stream(FeatCategory.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
                                .key("ability")
                                .name("Характеристика")
                                .type("filter")
                                .supportsMode(true)
                                .supportsUnion(true)
                                .values(Arrays.stream(Ability.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getShortName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
                                .key("repeatability")
                                .name("Повторяемость")
                                .type("singleton")
                                .supportsMode(false)
                                .supportsUnion(false)
                                .build()
                ))
                .build();
    }
}
