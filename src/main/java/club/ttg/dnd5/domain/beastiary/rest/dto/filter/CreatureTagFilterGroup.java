package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class CreatureTagFilterGroup extends AbstractFilterGroup<String, CreatureTagFilterGroup.CreatureTagFilterItem>
{
    private static final SimplePath<Object> PATH = Expressions.path(Object.class, "types");
    private static final StringPath NAME_PATH = QCreature.creature.name;

    public CreatureTagFilterGroup(final List<CreatureTagFilterItem> filters)
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

        final Set<String> positiveValues = getPositive();
        final Set<String> negativeValues = getNegative();

        final Function<String, BooleanExpression> typeTextContains = val -> Expressions.booleanTemplate(
                "(COALESCE({0} ->> 'text', '') ILIKE ('%%' || {1} || '%%'))",
                PATH, Expressions.constant(val)
        );

        // positive: (types.text contains tag) OR (name contains tag)
        final Function<String, BooleanExpression> positive = val ->
                typeTextContains.apply(val).or(NAME_PATH.containsIgnoreCase(val));

        // negative: NOT( (types.text contains tag) OR (name contains tag) )
        final Function<String, BooleanExpression> negative = val ->
                typeTextContains.apply(val).or(NAME_PATH.containsIgnoreCase(val)).not();

        final BooleanExpression positiveExpr = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : positiveValues.stream().map(positive).reduce(BooleanExpression::or).orElse(TRUE_EXPRESSION);

        final BooleanExpression negativeExpr = CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : negativeValues.stream().map(negative).reduce(BooleanExpression::and).orElse(TRUE_EXPRESSION);

        return positiveExpr.and(negativeExpr);
    }

    @Override
    public String getName()
    {
        return "Тег типа";
    }

    public static CreatureTagFilterGroup getDefault(final List<String> tags)
    {
        return new CreatureTagFilterGroup(
                tags.stream()
                        .map(CreatureTagFilterItem::new)
                        .sorted(Comparator.comparing(CreatureTagFilterItem::getName))
                        .collect(Collectors.toList())
        );
    }

    public static class CreatureTagFilterItem extends AbstractFilterItem<String>
    {
        public CreatureTagFilterItem(final String value)
        {
            super(StringUtils.capitalize(value), value, null);
        }
    }
}
