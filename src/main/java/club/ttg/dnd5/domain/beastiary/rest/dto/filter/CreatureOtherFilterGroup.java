package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.util.List;

public class CreatureOtherFilterGroup extends AbstractCustomQueryFilterGroup {
    public static final String NAME = "Прочее";

    public CreatureOtherFilterGroup(List<? extends AbstractCustomQueryFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static CreatureOtherFilterGroup getDefault() {
        return new CreatureOtherFilterGroup(List.of(new CreatureOtherFilterGroup.CreatureLairFilterSingleton(),
                new CreatureOtherFilterGroup.CreatureLegendaryActionFilterSingleton()));
    }

    public static class CreatureLegendaryActionFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Легендарное действие";

        public CreatureLegendaryActionFilterSingleton() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate("legendary_action >= 1");
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate("legendary_action < 1 or legendary_action is null");
        }
    }

    public static class CreatureLairFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Логово";

        public CreatureLairFilterSingleton() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate("lair is not null and trim(lair) <> ''");
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate("lair is null or trim(lair) = ''");
        }
    }
}
