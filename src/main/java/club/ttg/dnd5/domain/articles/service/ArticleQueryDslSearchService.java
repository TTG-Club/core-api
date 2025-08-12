package club.ttg.dnd5.domain.articles.service;

import club.ttg.dnd5.domain.articles.model.Article;
import club.ttg.dnd5.domain.articles.model.QArticle;
import club.ttg.dnd5.domain.filter.service.AbstractQueryDslSearchService;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class ArticleQueryDslSearchService extends AbstractQueryDslSearchService<Article, QArticle> {
    private static final QArticle ARTICLE = QArticle.article;
    private static final OrderSpecifier<?>[] ORDER = new OrderSpecifier[]{ARTICLE.categories.asc(), ARTICLE.name.asc()};

    public ArticleQueryDslSearchService(ArticlesFilterServise articlesFilterService, EntityManager entityManager) {
        super(articlesFilterService, entityManager, ARTICLE);
    }

    @Override
    protected OrderSpecifier<?>[] getOrder() {
        return ORDER;
    }
}
