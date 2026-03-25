package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackgroundFilterService extends AbstractSavedFilterService {
    private final BackgroundRepository backgroundRepository;

    public BackgroundFilterService(SourceSavedFilterService sourceSavedFilterService,
                                  BackgroundRepository backgroundRepository) {
        super(sourceSavedFilterService);
        this.backgroundRepository = backgroundRepository;
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
    protected club.ttg.dnd5.domain.filter.model.FilterInfo buildDefaultFilterInfo() {
        return new club.ttg.dnd5.domain.filter.model.FilterInfo(java.util.Collections.emptyList());
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.builder()
                .sources(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(sourceSavedFilterService.getFilter()).getSources())
                .filters(List.of(
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("ability")
                                .name("Характеристики")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.common.dictionary.Ability.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build(),
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("skill")
                                .name("Навыки")
                                .type("threeState")
                                .values(java.util.Arrays.stream(club.ttg.dnd5.domain.common.dictionary.Skill.values())
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}
