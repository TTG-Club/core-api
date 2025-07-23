package club.ttg.dnd5.domain.articles.service;

import club.ttg.dnd5.domain.articles.model.filter.ArticleSavedFilter;
import club.ttg.dnd5.domain.articles.repository.ArticleFilterRepository;
import club.ttg.dnd5.domain.articles.rest.dto.filter.ArticleTagFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.repository.SavedFilterRepository;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleFilterService extends AbstractSavedFilterService<ArticleSavedFilter> {
    private static final String FILTER_VERSION = "1.0";
    private final ArticleFilterRepository glossaryFilterRepository;

    public ArticleFilterService(SavedFilterRepository<ArticleSavedFilter> savedFilterRepository, UserService userService, ArticleFilterRepository glossaryFilterRepository) {
        super(savedFilterRepository, userService);
        this.glossaryFilterRepository = glossaryFilterRepository;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        List<String> categories = glossaryFilterRepository.findDistinctTagCategories();

        return new FilterInfo(List.of(
                ArticleTagFilterGroup.getDefault(categories)
        ), FILTER_VERSION);
    }
}
