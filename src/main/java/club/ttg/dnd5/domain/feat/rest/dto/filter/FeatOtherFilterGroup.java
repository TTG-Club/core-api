package club.ttg.dnd5.domain.feat.rest.dto.filter;

import club.ttg.dnd5.domain.magic.rest.dto.filter.MagicItemOtherFilterGroup;
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
@JsonTypeName("f-oth")
public class FeatOtherFilterGroup extends AbstractCustomQueryFilterGroup
{
    public static final String NAME = "Прочее";

    public FeatOtherFilterGroup(final List<? extends AbstractCustomQueryFilterItem> filters)
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
                    new FeatOtherFilterGroup.RepeatableFilterSingleton()
                )
        );
    }
    @JsonTypeName("f-oth-s")
    public static class RepeatableFilterSingleton extends AbstractCustomQueryFilterItem
    {
        private static final String NAME = "Повторяемая";

        public RepeatableFilterSingleton()
        {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery()
        {
            return Expressions.booleanTemplate("repeatability IS TRUE");
        }

        @Override
        public BooleanExpression getNegativeQuery()
        {
            return Expressions.booleanTemplate("repeatability IS NOT TRUE");
        }
    }
}
