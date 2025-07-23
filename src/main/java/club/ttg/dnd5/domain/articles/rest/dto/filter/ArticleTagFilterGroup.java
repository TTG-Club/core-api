package club.ttg.dnd5.domain.articles.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArticleTagFilterGroup extends AbstractFilterGroup<String, ArticleTagFilterGroup.ArticleTagFilterItem> {

    public ArticleTagFilterGroup(List<ArticleTagFilterGroup.ArticleTagFilterItem> filters) {
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
                "(traits is not null and exists (select 1 from jsonb_array_elements(traits) as elem where %s))",
                positiveValues.stream()
                        .map(val -> String.format("elem @> '\"%s\"'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        Set<String> negativeValues = getNegative();
        BooleanExpression negativeExpr = CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format(
                "(traits is not null and not exists (select 1 from jsonb_array_elements(traits) as elem where %s))",
                negativeValues.stream()
                        .map(val -> String.format("elem @> '\"%s\"'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        return positiveExpr.and(negativeExpr);
    }

    @Override
    public String getName() {
        return "Теги";
    }

    public static ArticleTagFilterGroup getDefault(List<String> traitNames) {
        return new ArticleTagFilterGroup(
                traitNames.stream()
                        .map(ArticleTagFilterGroup.ArticleTagFilterItem::new)
                        .collect(Collectors.toList())
        );
    }

    public static class ArticleTagFilterItem extends AbstractFilterItem<String> {
        public ArticleTagFilterItem(String value) {
            super(value, value, null);
        }
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}
