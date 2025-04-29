package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;

import java.util.List;

public class SpellComponentsFilterGroup extends AbstractCustomQueryFilterGroup {
    public static final String NAME = "Компоненты";
    protected static final BooleanPath vPath = Expressions.booleanPath(QSpell.spell, "v");
    protected static final BooleanPath sPath = Expressions.booleanPath(QSpell.spell, "s");
    protected static final SimplePath<MaterialComponent> mPath = Expressions.simplePath(MaterialComponent.class, QSpell.spell, "m");

    @Override
    public String getName() {
        return NAME;
    }

    public SpellComponentsFilterGroup(List<? extends AbstractCustomQueryFilterItem> filters) {
        super(filters);
    }


    public static SpellComponentsFilterGroup getDefault() {
        return new SpellComponentsFilterGroup(List.of(new SpellVerbalComponentFilterItem(), new SpellSomaticComponentFilterItem(),
                new SpellMaterialComponentFilterItem(), new SpellMaterialConsumableFilterItem(),
                new SpellMaterialWithCostFilterItem()));
    }

    public static class SpellSomaticComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "соматический";

        public SpellSomaticComponentFilterItem() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return sPath.isTrue();
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return sPath.isFalse();
        }
    }

    public static class SpellVerbalComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "вербальный";

        public SpellVerbalComponentFilterItem() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return vPath.isTrue();
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return vPath.isFalse();
        }
    }

    public static class SpellMaterialComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "материальный";

        public SpellMaterialComponentFilterItem() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return mPath.isNotNull();
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return mPath.isNull();
        }
    }

    public static class SpellMaterialConsumableFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "расходуемый";
        public static final String EXPRESSION_TEMPLATE = "%s  @> '{\"consumable\": %s}'";

        public SpellMaterialConsumableFilterItem() {
            super(NAME, null);
        }


        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate(String.format(EXPRESSION_TEMPLATE, mPath, true));
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate(String.format(EXPRESSION_TEMPLATE, mPath, false));
        }

    }

    public static class SpellMaterialWithCostFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "со стоимостью";
        public static final String EXPRESSION_TEMPLATE = "%s  @> '{\"withCost\": %s}'";

        public SpellMaterialWithCostFilterItem() {
            super(NAME, null);
        }

        @Override
        public BooleanExpression getPositiveQuery() {
            return Expressions.booleanTemplate(String.format(EXPRESSION_TEMPLATE, mPath, true));
        }

        @Override
        public BooleanExpression getNegativeQuery() {
            return Expressions.booleanTemplate(String.format(EXPRESSION_TEMPLATE, mPath, false));
        }

    }
}
