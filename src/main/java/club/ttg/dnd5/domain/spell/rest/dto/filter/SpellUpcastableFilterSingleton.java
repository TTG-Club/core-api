package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractCustomQueryFilterSingleton;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;

public class SpellUpcastableFilterSingleton extends AbstractCustomQueryFilterSingleton {

    private static final String NAME = "Улучшается с уровнем ячейки";
    private static final BooleanPath PATH = QSpell.spell.upcastable;

    public SpellUpcastableFilterSingleton() {
        super(NAME, null);
    }

    public static SpellUpcastableFilterSingleton getDefault() {
        return new SpellUpcastableFilterSingleton();
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
