package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
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
        public CreatureLairFilterSingleton() { super(NAME, null); }

        @Override
        public BooleanExpression getPositiveQuery() {
            // есть логово с непустым именем
            return Expressions.booleanTemplate(
                    "{0} is not null and {0} ->> 'name' is not null and btrim({0} ->> 'name') <> ''",
                    QCreature.creature.lair
            );
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            // нет логова ИЛИ имя пустое
            return Expressions.booleanTemplate(
                    "{0} is null or {0} ->> 'name' is null or btrim({0} ->> 'name') = ''",
                    QCreature.creature.lair
            );
        }
    }
}
