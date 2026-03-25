package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.glossary.repository.GlossaryFilterRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.filter.GlossaryTagCategoryFilterGroup;
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
    public SearchBody getDefaultFilterInfo()
    {
        List<String> usedSourceCodes = glossaryFilterRepository.findAllUsedSourceCodes();

        return new SearchBody(
                sourceSavedFilterService.getDefaultFilterInfo(usedSourceCodes),
                buildDefaultFilterInfo()
        );
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        List<String> categories = glossaryFilterRepository.findDistinctTagCategories();

        return new FilterInfo(List.of(
                GlossaryTagCategoryFilterGroup.getDefault(categories)
        ));
    }
}
