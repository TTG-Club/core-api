package club.ttg.dnd5.domain.background.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class AbilityFilterGroup extends AbstractFilterGroup<Ability, AbilityFilterGroup.AbilityFilterItem>
{
    public AbilityFilterGroup(List<AbilityFilterItem> filters)
    {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery()
    {
        if (isSingular())
        {
            return TRUE_EXPRESSION;
        }

        BooleanExpression result = TRUE_EXPRESSION;

        Set<Ability> positive = getPositive();
        if (CollectionUtils.isNotEmpty(positive))
        {
            String jsonArray = toJsonArray(positive);
            result = result.and(Expressions.booleanTemplate(
                    "abilities @> cast({0} as jsonb)", jsonArray
            ));
        }

        Set<Ability> negative = getNegative();
        if (CollectionUtils.isNotEmpty(negative))
        {
            for (Ability ability : negative)
            {
                String single = toJsonArray(List.of(ability));
                result = result.and(Expressions.booleanTemplate(
                        "not (abilities @> cast({0} as jsonb))", single
                ));
            }
        }

        return result;
    }

    @Override
    public String getName()
    {
        return "Характеристики";
    }

    public static AbilityFilterGroup getDefault()
    {
        return new AbilityFilterGroup(
                Arrays.stream(Ability.values())
                        .map(AbilityFilterItem::new)
                        .collect(Collectors.toList())
        );
    }

    public static class AbilityFilterItem extends AbstractFilterItem<Ability>
    {
        public AbilityFilterItem(Ability value)
        {
            super(value.getName(), value, null);
        }
    }

    private static String toJsonArray(Collection<Ability> values)
    {
        return values.stream()
                .map(Ability::toString)
                .map(s -> "\"" + s + "\"")     // "STR"
                .collect(Collectors.joining(",", "[", "]")); // ["STR","DEX"]
    }
}
