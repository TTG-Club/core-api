package club.ttg.dnd5.domain.glossary.rest.dto.filter;

import club.ttg.dnd5.domain.glossary.model.QGlossary;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonTypeName("g-tag")
public class GlossaryTagCategoryFilterGroup extends AbstractFilterGroup<String, GlossaryTagCategoryFilterGroup.GlossaryTagCategoryFilterItem> {

    public GlossaryTagCategoryFilterGroup(List<GlossaryTagCategoryFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Категория тега";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<String> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : QGlossary.glossary.tagCategory.in(positiveValues);

        Set<String> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : QGlossary.glossary.tagCategory.notIn(negativeValues));
    }

    public static GlossaryTagCategoryFilterGroup getDefault(List<String> availableCategories) {
        return new GlossaryTagCategoryFilterGroup(
                availableCategories.stream()
                        .map(GlossaryTagCategoryFilterItem::new)
                        .collect(Collectors.toList())
        );
    }

    @JsonTypeName("g-tag-i")
    public static class GlossaryTagCategoryFilterItem extends AbstractFilterItem<String> {
        public GlossaryTagCategoryFilterItem(String value) {
            super(value, value, null);
        }
    }
}