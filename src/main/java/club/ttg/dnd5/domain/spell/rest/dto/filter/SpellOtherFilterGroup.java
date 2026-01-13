package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;

import java.util.List;

@FilterRegistry
@JsonTypeName("s-oth")
public class SpellOtherFilterGroup extends AbstractCustomQueryFilterGroup {
    public static final String NAME = "Прочее";

    @Override
    public String getName() {
        return NAME;
    }

    public SpellOtherFilterGroup(List<? extends AbstractCustomQueryFilterItem> filters) {
        super(filters);
    }

    public static SpellOtherFilterGroup getDefault() {
        return new SpellOtherFilterGroup(List.of(new SpellRitualFilterSingleton(),
                new SpellConcentrationFilterSingleton(),
                new SpellUpcastableFilterSingleton()));
    }

    @FilterRegistry
    @JsonTypeName("s-oth-rtl")
    public static class SpellRitualFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Ритуал";

        public SpellRitualFilterSingleton() {
            super(NAME, null);
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

    @JsonTypeName("s-oth-con")
    public static class SpellConcentrationFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Концентрация";

        public SpellConcentrationFilterSingleton() {
            super(NAME, null);
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

    @JsonTypeName("s-oth-upc")
    public static class SpellUpcastableFilterSingleton extends AbstractCustomQueryFilterItem {
        private static final String NAME = "Улучшается с уровнем ячейки";
        private static final BooleanPath PATH = QSpell.spell.upcastable;

        public SpellUpcastableFilterSingleton() {
            super(NAME, null);
        }


        @Override
        public BooleanExpression getPositiveQuery() {
            return PATH.isTrue();
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return PATH.isFalse();
        }
    }
}
