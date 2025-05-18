package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.glossary.model.filter.GlossarySavedFilter;
import club.ttg.dnd5.domain.glossary.repository.GlossarySavedFilterRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.filter.GlossaryTagCategoryFilterGroup;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlossaryFilterService extends AbstractSavedFilterService<GlossarySavedFilter> {
    private static final String FILTER_VERSION = "1.0";
    private final GlossarySavedFilterRepository glossaryRepo;

    public GlossaryFilterService(GlossarySavedFilterRepository glossarySavedFilterRepository,
                                 UserService userService) {
        super(glossarySavedFilterRepository, userService);
        this.glossaryRepo = glossarySavedFilterRepository;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        List<String> categories = glossaryRepo.findDistinctTagCategories();

        return new FilterInfo(List.of(
                GlossaryTagCategoryFilterGroup.getDefault(categories)
        ), FILTER_VERSION);
    }
}
