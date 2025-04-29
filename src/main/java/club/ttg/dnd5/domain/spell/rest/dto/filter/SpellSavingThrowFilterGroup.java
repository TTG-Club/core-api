package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Ability;
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
public class SpellSavingThrowFilterGroup extends AbstractFilterGroup<Ability, SpellSavingThrowFilterGroup.SpellSavingThrowFilterItem> {


    public SpellSavingThrowFilterGroup(List<SpellSavingThrowFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Спасброски";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Ability> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate("jsonb_exists_any(spell.saving_throw,  {0} ::text[])", Expressions.constant(positiveValues.stream()
                .map(Enum::name)
                .toArray(String[]::new)));
        Set<Ability> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate("jsonb_exists_any(spell.saving_throw,  {0} ::text[]) is not true", Expressions.constant(negativeValues.stream()
                .map(Enum::name)
                .toArray(String[]::new))));
    }

    public static SpellSavingThrowFilterGroup getDefault() {
        return new SpellSavingThrowFilterGroup(Arrays.stream(Ability.values()).
                map(SpellSavingThrowFilterItem::new)
                .collect(Collectors.toList()));
    }

    public static class SpellSavingThrowFilterItem extends AbstractFilterItem<Ability> {
        public SpellSavingThrowFilterItem(Ability value) {
            super(value.getName(), value, null);
        }
    }


}
