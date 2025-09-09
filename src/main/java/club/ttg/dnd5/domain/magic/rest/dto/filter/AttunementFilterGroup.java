package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.rest.dto.filter.CreatureOtherFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttunementFilterGroup extends AbstractCustomQueryFilterGroup {
    public static final String NAME = "Прочее";

    public AttunementFilterGroup(final List<? extends AbstractCustomQueryFilterItem> filters) {
        super(filters);
    }


    @Override
    public String getName() {
        return NAME;
    }

    public static CreatureOtherFilterGroup getDefault() {
        return new CreatureOtherFilterGroup(
            List.of(
                new AttunementFilterGroup.AttunementTrueFilterSingleton(),
                new ChargesFilterSingleton())
        );
    }

    public static class AttunementTrueFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Настройка";

        public AttunementTrueFilterSingleton() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate("attunement ->> requires == 'true'");
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate("attunement ->> requires == 'false'");
        }
    }

    public static class ChargesFilterSingleton extends AbstractCustomQueryFilterItem {

        private static final String NAME = "Заряды";

        public ChargesFilterSingleton() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate("charges > 0");
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate("charges = 0 OR charges IS NULL");
        }
    }
}
