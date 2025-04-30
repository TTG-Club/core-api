package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterSingleton;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

public class SpellConcentrationFilterSingleton extends AbstractCustomQueryFilterSingleton {

    private static final String NAME = "Концентрация";

    public SpellConcentrationFilterSingleton() {
        super(NAME, null);
    }

    public static SpellConcentrationFilterSingleton getDefault() {
        return new SpellConcentrationFilterSingleton();
    }

    @Override
    public BooleanExpression getPositiveQuery() {
        return Expressions.booleanTemplate("exists (select 1 from jsonb_array_elements(duration) as elem where (elem  @> '{\"concentration\": true}'))");
    }

    @Override
    public BooleanExpression getNegativeQuery() {
        return Expressions.booleanTemplate("exists (select 1 from jsonb_array_elements(duration) as elem where (elem  @> '{\"concentration\": false}'))");
    }
}
