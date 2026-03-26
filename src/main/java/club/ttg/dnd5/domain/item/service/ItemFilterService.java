package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;

import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemFilterService extends AbstractSavedFilterService {

    public ItemFilterService(SourceSavedFilterService sourceSavedFilterService) {
        super(sourceSavedFilterService);
    }

    // legacy (deprecated)
    @Override
    @Deprecated
    public SearchBody getDefaultFilterInfo()
    {
        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(),
                buildDefaultFilterInfo()
        );
    }

    @Override
    @Deprecated
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(java.util.Collections.emptyList());
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.builder()
                .sources(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(sourceSavedFilterService.getFilter()).getSources())
                .filters(List.of(
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("types")
                                .name("Категория")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.item.model.ItemType.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .sorted(java.util.Comparator.comparing(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta::getName))
                                        .toList())
                                .build()
                ))
                .build();
    }
}
