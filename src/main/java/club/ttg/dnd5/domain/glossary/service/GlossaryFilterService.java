package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.glossary.repository.GlossaryFilterRepository;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlossaryFilterService extends AbstractSavedFilterService {
    private final GlossaryFilterRepository glossaryFilterRepository;

    public GlossaryFilterService(SourceSavedFilterService sourceSavedFilterService, GlossaryFilterRepository glossaryFilterRepository) {
        super(sourceSavedFilterService);
        this.glossaryFilterRepository = glossaryFilterRepository;
    }

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
        List<String> categories = glossaryFilterRepository.findDistinctTagCategories();

        return club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.builder()
                .sources(club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataMapper.map(sourceSavedFilterService.getFilter()).getSources())
                .filters(List.of(
                        club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterGroupMeta.builder()
                                .key("tagCategory")
                                .name("Категория тега")
                                .type("threeState")
                                .values(categories.stream()
                                        .map(v -> club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse.FilterValueMeta.builder()
                                                .name(v).value(v).build())
                                        .toList())
                                .build()
                ))
                .build();
    }
}
