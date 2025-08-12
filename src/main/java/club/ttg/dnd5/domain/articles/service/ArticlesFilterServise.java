package club.ttg.dnd5.domain.articles.service;

import club.ttg.dnd5.domain.articles.model.filter.ArticleSavedFilter;
import club.ttg.dnd5.domain.articles.repository.ArticleSavedFilterRepository;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.service.AbstractSavedFilterService;
import club.ttg.dnd5.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticlesFilterServise extends AbstractSavedFilterService<ArticleSavedFilter> {
    private static final String FILTER_VERSION = "1.0";


    public ArticlesFilterServise(ArticleSavedFilterRepository articleSavedFilterRepository,
                                 UserService userService) {
        super(articleSavedFilterRepository, userService);
    }

    @Override
    protected FilterInfo buildDefaultFilterInfo() {
        return new FilterInfo(List.of(
        ), FILTER_VERSION);
    }
}
