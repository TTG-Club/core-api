package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class SpellConditionFilterGroup extends AbstractFilterGroup<Condition, SpellConditionFilterGroup.SpellConditionFilterItem> {

    public SpellConditionFilterGroup(List<SpellConditionFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Накладываемые состояния";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Condition> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate("jsonb_exists_any(spell.condition,  {0} ::text[])", Expressions.constant(positiveValues.stream()
                .map(Enum::name)
                .toArray(String[]::new)));
        Set<Condition> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate("jsonb_exists_any(spell.condition,  {0} ::text[]) is not true", Expressions.constant(negativeValues.stream()
                .map(Enum::name)
                .toArray(String[]::new))));
    }

    public static SpellConditionFilterGroup getDefault() {
        return new SpellConditionFilterGroup(Arrays.stream(Condition.values())
                .map(SpellConditionFilterItem::new)
                .collect(Collectors.toList()));
    }

    public static class SpellConditionFilterItem extends AbstractFilterItem<Condition> {
        public SpellConditionFilterItem(Condition value) {
            super(value.getName(), value, null);
        }
    }

}
