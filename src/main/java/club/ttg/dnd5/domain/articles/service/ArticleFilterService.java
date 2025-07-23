package club.ttg.dnd5.domain.articles.service;

import club.ttg.dnd5.domain.articles.model.filter.ArticleSavedFilter;
import club.ttg.dnd5.domain.articles.repository.ArticleRepository;
import club.ttg.dnd5.domain.articles.repository.ArticleSavedFilterRepository;
import club.ttg.dnd5.domain.articles.rest.dto.filter.ArticleCategoriesFilterGroup;
import club.ttg.dnd5.domain.articles.rest.dto.filter.ArticleTagFilterGroup;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleFilterService extends AbstractSavedFilterService<ArticleSavedFilter> {
    private static final String FILTER_VERSION = "1.0";

    private final ArticleRepository articleRepository;


    public ArticleFilterService(ArticleSavedFilterRepository articleSavedFilterRepository,
                                UserService userService, ArticleRepository articleRepository) {
        super(articleSavedFilterRepository, userService);
        this.articleRepository = articleRepository;
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
                ArticleTagFilterGroup.getDefault(articleRepository),
                ArticleCategoriesFilterGroup.getDefault(articleRepository)
        ), FILTER_VERSION);
    }
}
