package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonTypeName("c-tra")
public class CreatureTraitsFilterGroup extends AbstractFilterGroup<String, CreatureTraitsFilterGroup.CreatureTraitsFilterItem> {

    public CreatureTraitsFilterGroup(List<CreatureTraitsFilterItem> filters) {
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
                        .map(val -> String.format("elem->>'name' = '%s'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        Set<String> negativeValues = getNegative();
        BooleanExpression negativeExpr = CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format(
                "(traits is not null and not exists (select 1 from jsonb_array_elements(traits) as elem where %s))",
                negativeValues.stream()
                        .map(val -> String.format("elem->>'name' = '%s'", escape(val)))
                        .collect(Collectors.joining(" or "))
        ));

        return positiveExpr.and(negativeExpr);
    }

    @Override
    public String getName() {
        return "Умения";
    }

    public static CreatureTraitsFilterGroup getDefault(List<String> traitNames) {
        return new CreatureTraitsFilterGroup(
                traitNames.stream()
                        .map(CreatureTraitsFilterItem::new)
                        .sorted(Comparator.comparing(CreatureTraitsFilterItem::getName))
                        .collect(Collectors.toList())
        );
    }

    @JsonTypeName("c-tra-i")
    public static class CreatureTraitsFilterItem extends AbstractFilterItem<String> {
        public CreatureTraitsFilterItem(String value) {
            super(value, value, null);
        }
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}
