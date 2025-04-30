package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterSingleton;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

public class SpellRitualFilterSingleton extends AbstractCustomQueryFilterSingleton {

    private static final String NAME = "Ритуал";

    public SpellRitualFilterSingleton() {
        super(NAME, null);
    }

    public static SpellRitualFilterSingleton getDefault() {
        return new SpellRitualFilterSingleton();
    }

    @Override
    public BooleanExpression getPositiveQuery() {
        return Expressions.booleanTemplate("exists (select 1 from jsonb_array_elements(casting_time) as elem where (elem  @> '{\"unit\": \"RITUAL\"}'))");
    }

    @Override
    public BooleanExpression getNegativeQuery() {
        return Expressions.booleanTemplate("exists (select 1 from jsonb_array_elements(casting_time) as elem where not (elem  @> '{\"unit\": \"RITUAL\"}'))");
    }
}
