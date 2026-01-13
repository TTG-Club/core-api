package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;

import java.util.List;

@JsonTypeName("s-comp")
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

    @JsonTypeName("s-comp-s")
    public static class SpellSomaticComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "Соматический";

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

    @JsonTypeName("s-comp-v")
    public static class SpellVerbalComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "Вербальный";

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
    @JsonTypeName("s-comp-s")
    public static class SpellMaterialComponentFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "Материальный";

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
    @JsonTypeName("s-comp-c")
    public static class SpellMaterialConsumableFilterItem extends AbstractCustomQueryFilterItem {
        public static final String NAME = "Расходуемый";
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
    @JsonTypeName("s-comp-wc")
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
