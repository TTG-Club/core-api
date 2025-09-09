package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttunementFilterGroup extends AbstractCustomQueryFilterGroup
{
    public static final String NAME = "Прочее";

    public AttunementFilterGroup(final List<? extends AbstractCustomQueryFilterItem> filters)
    {
        super(filters);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    /** Factory with both filters enabled */
    public static AttunementFilterGroup getDefault()
    {
        return new AttunementFilterGroup(
                List.of(
                        new AttunementTrueFilterSingleton(),
                        new ChargesFilterSingleton()
                )
        );
    }

    /** Filters items that require attunement */
    public static class AttunementTrueFilterSingleton extends AbstractCustomQueryFilterItem
    {
        private static final String NAME = "Настройка";

        public AttunementTrueFilterSingleton()
        {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery()
        {
            return Expressions.booleanTemplate(
                    "attunement @> cast('{\"requires\": true}' as jsonb)"
            );
        }

        @Override
        public BooleanExpression getNegativeQuery()
        {
            return Expressions.booleanTemplate(
                    "NOT (attunement @> cast('{\"requires\": true}' as jsonb))"
            );
        }
    }

    /** Filters by charges count */
    public static class ChargesFilterSingleton extends AbstractCustomQueryFilterItem
    {
        private static final String NAME = "Заряды";

        public ChargesFilterSingleton()
        {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery()
        {
            return Expressions.booleanTemplate("coalesce(charges, 0) > 0");
        }

        @Override
        public BooleanExpression getNegativeQuery()
        {
            return Expressions.booleanTemplate("coalesce(charges, 0) = 0");
        }
    }
}
