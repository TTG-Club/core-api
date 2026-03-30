package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.rest.dto.FilterKeys;
import club.ttg.dnd5.domain.item.rest.dto.ItemQueryRequest;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper;
import club.ttg.dnd5.domain.filter.rest.dto.SupportsConfig;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemFilterService
{
    private final SourceSavedFilterService sourceSavedFilterService;

    public FilterMetadataResponse getFilterMetadata(Set<String> selectedSources)
    {
        return FilterMetadataResponse.builder()
                .sources(FilterMetadataMapper.mapSourcesFromFilterInfo(sourceSavedFilterService.getDefaultFilterInfo(selectedSources)))
                .filters(List.of(
                        FilterGroupMeta.builder()
                                .key(FilterKeys.keyOf(ItemQueryRequest.class, "itemType"))
                                .name("Категория")
                                .supports(SupportsConfig.builder().mode(true).union(false).build())
                                .values(Arrays.stream(ItemType.values())
                                        .map(v -> FilterValueMeta.builder()
                                                .id(v.name())
                                                .value(v.name())
                                                .name(v.getName())
                                                .build())
                                        .sorted(Comparator.comparing(FilterValueMeta::getName))
                                        .toList())
                                .build()
                ))
                .build();
    }
}
