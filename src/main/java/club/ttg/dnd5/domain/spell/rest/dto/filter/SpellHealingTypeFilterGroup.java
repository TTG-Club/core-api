package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.HealingType;
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
public class SpellHealingTypeFilterGroup extends AbstractFilterGroup<HealingType, SpellHealingTypeFilterGroup.SpellHealingTypeFilterItem> {


    public SpellHealingTypeFilterGroup(List<SpellHealingTypeFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Тип лечения";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<HealingType> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate("jsonb_exists_any(spell.healing_type,  {0} ::text[])", Expressions.constant(positiveValues.stream()
                .map(Enum::name)
                .toArray(String[]::new)));
        Set<HealingType> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate("jsonb_exists_any(spell.healing_type,  {0} ::text[]) is not true", Expressions.constant(negativeValues.stream()
                .map(Enum::name)
                .toArray(String[]::new))));
    }

    public static SpellHealingTypeFilterGroup getDefault() {
        return new SpellHealingTypeFilterGroup(Arrays.stream(HealingType.values()).
                map(SpellHealingTypeFilterItem::new)
                .collect(Collectors.toList()));
    }

    public static class SpellHealingTypeFilterItem extends AbstractFilterItem<HealingType> {
        public SpellHealingTypeFilterItem(HealingType value) {
            super(value.getName(), value, null);
        }
    }
}
