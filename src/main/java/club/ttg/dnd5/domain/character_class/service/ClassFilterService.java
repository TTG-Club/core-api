package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClassFilterService extends AbstractSavedFilterService
{
    public ClassFilterService(
            SourceSavedFilterService sourceSavedFilterService
    )
    {
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
    protected club.ttg.dnd5.domain.filter.model.FilterInfo buildDefaultFilterInfo()
    {
        return new club.ttg.dnd5.domain.filter.model.FilterInfo(java.util.Collections.emptyList());
    }

    @Override
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilterMetadata() {
        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.builder()
                .sources(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(sourceSavedFilterService.getFilter()).getSources())
                .filters(List.of(
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("hitDie")
                                .name("Кость хитов")
                                .type("threeState")
                                .values(java.util.stream.Stream.of(
                                                club.ttg.dnd5.domain.common.dictionary.Dice.d6,
                                                club.ttg.dnd5.domain.common.dictionary.Dice.d8,
                                                club.ttg.dnd5.domain.common.dictionary.Dice.d10,
                                                club.ttg.dnd5.domain.common.dictionary.Dice.d12)
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v.getName()).value(v.name()).build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}