package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.FilterGroupType;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
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
public class BackgroundFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata()
    {
        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo()))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key("ability")
                                .name("Характеристики")
                                .type(FilterGroupType.FILTER)
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(Ability.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build(),
                        FilterGroupMeta.builder()
                                .key("skill")
                                .name("Навыки")
                                .type(FilterGroupType.FILTER)
                                .supports(SupportsConfig.builder().mode(true).union(true).build())
                                .values(Arrays.stream(Skill.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}
