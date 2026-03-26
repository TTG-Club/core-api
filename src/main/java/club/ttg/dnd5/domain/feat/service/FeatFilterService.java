package club.ttg.dnd5.domain.feat.service;


import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatFilterService extends AbstractSavedFilterService {

    public FeatFilterService(SourceSavedFilterService sourceSavedFilterService) {
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
                                .key("category")
                                .name("Категория")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.feat.model.FeatCategory.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build(),
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("ability")
                                .name("Характеристика")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.common.dictionary.Ability.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getShortName()).value(v.name()).build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}
