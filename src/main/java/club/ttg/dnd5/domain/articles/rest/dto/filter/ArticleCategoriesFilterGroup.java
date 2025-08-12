package club.ttg.dnd5.domain.articles.rest.dto.filter;

import club.ttg.dnd5.domain.articles.repository.ArticleRepository;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArticleCategoriesFilterGroup extends AbstractFilterGroup<String, ArticleCategoriesFilterGroup.ArticleCategoriesFilterItem> {

    public ArticleCategoriesFilterGroup(List<ArticleCategoriesFilterGroup.ArticleCategoriesFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<String> positiveValues = getPositive();
        BooleanExpression positiveExpr = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format(
                "(articles is not null and exists (select 1 from jsonb_array_elements(articles) as elem where %s))",
                positiveValues.stream()
                        .map(val -> String.format("elem @> '\"%s\"'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        Set<String> negativeValues = getNegative();
        BooleanExpression negativeExpr = CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format(
                "(articles is not null and not exists (select 1 from jsonb_array_elements(articles) as elem where %s))",
                negativeValues.stream()
                        .map(val -> String.format("elem @> '\"%s\"'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        return positiveExpr.and(negativeExpr);
    }

    @Override
    public String getName() {
        return "Категория";
    }

//    public static ArticleCategoriesFilterGroup getDefault(ArticleRepository articleRepository) {
//        List<String> allCategories = articleRepository.findAllUniqueCategories();
//        return new ArticleCategoriesFilterGroup(
//                allCategories.stream()
//                        .map(ArticleCategoriesFilterGroup.ArticleCategoriesFilterItem::new)
//                        .collect(Collectors.toList())
//        );
//    }

    public static class ArticleCategoriesFilterItem extends AbstractFilterItem<String> {
        public ArticleCategoriesFilterItem(String value) {
            super(value, value, null);
        }
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}
