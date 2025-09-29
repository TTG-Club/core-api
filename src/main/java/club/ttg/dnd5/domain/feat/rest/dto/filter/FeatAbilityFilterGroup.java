package club.ttg.dnd5.domain.feat.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class FeatAbilityFilterGroup extends AbstractFilterGroup<Ability, FeatAbilityFilterGroup.FeatAbilityFilterItem>
{
    private static final SimplePath<Object> PATH = Expressions.path(Object.class, "abilities");

    public FeatAbilityFilterGroup(List<FeatAbilityFilterItem> filters)
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

        Set<Ability> positiveValues = getPositive();
        BooleanExpression result = TRUE_EXPRESSION;

        // AND по каждому положительному значению
        if (!CollectionUtils.isEmpty(positiveValues))
        {
            for (Ability ability : positiveValues)
            {
                result = result.and(
                        Expressions.booleanTemplate("{0} @> cast({1} as jsonb)",
                                PATH,
                                "[\"" + ability.toString() + "\"]")
                );
            }
        }

        // исключения (NOT IN) через AND
        Set<Ability> negativeValues = getNegative();
        if (!CollectionUtils.isEmpty(negativeValues))
        {
            for (Ability ability : negativeValues)
            {
                result = result.and(
                        Expressions.booleanTemplate("not ({0} @> cast({1} as jsonb))",
                                PATH,
                                "[\"" + ability.toString() + "\"]")
                );
            }
        }

        return result;
    }

    @Override
    public String getName()
    {
        return "Улучшаемые характеристики";
    }

    public static FeatAbilityFilterGroup getDefault()
    {
        return new FeatAbilityFilterGroup(
                Arrays.stream(Ability.values())
                        .map(FeatAbilityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class FeatAbilityFilterItem extends AbstractFilterItem<Ability>
    {
        public FeatAbilityFilterItem(Ability value)
        {
            super(value.getName(), value, null);
        }
    }
}