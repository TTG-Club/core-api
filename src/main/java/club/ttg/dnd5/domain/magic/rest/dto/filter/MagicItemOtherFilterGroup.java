package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonTypeName("mi-oth")
public class MagicItemOtherFilterGroup extends AbstractCustomQueryFilterGroup
{
    public static final String NAME = "Прочее";

    public MagicItemOtherFilterGroup(final List<? extends AbstractCustomQueryFilterItem> filters)
    {
        super(filters);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public static MagicItemOtherFilterGroup getDefault()
    {
        return new MagicItemOtherFilterGroup(
                List.of(
                        new AttunementTrueFilterSingleton(),
                        new ChargesFilterSingleton(),
                        new CurseFilterSingleton(),
                        new ConsumableFilterSingleton()
                )
        );
    }

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
    @JsonTypeName("mi-oth-chr")
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

    @JsonTypeName("mi-oth-cur")
    public static class CurseFilterSingleton extends AbstractCustomQueryFilterItem
    {
        private static final String NAME = "Проклятие";

        public CurseFilterSingleton()
        {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery()
        {
            // boolean column; NULL-safe
            return Expressions.booleanTemplate("curse IS TRUE");
        }

        @Override
        public BooleanExpression getNegativeQuery()
        {
            // false OR NULL considered as "not cursed"
            return Expressions.booleanTemplate("curse IS NOT TRUE");
        }
    }

    @JsonTypeName("mi-oth-cons")
    public static class ConsumableFilterSingleton extends AbstractCustomQueryFilterItem
    {
        private static final String NAME = "Расходуемый";

        public ConsumableFilterSingleton()
        {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery()
        {
            // boolean column; NULL-safe
            return Expressions.booleanTemplate("consumable IS TRUE");
        }

        @Override
        public BooleanExpression getNegativeQuery()
        {
            return Expressions.booleanTemplate("consumable IS NOT TRUE");
        }
    }
}
