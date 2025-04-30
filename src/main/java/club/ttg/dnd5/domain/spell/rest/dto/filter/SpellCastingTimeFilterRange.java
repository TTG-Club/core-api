package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.AbstractFilterRange;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpellCastingTimeFilterRange extends AbstractFilterRange<SpellCastingTime, SpellCastingTimeFilterRange.SpellCastingTimeFilterItem> {

    private static final String NAME = "Время накладывания";
    private static final List<SpellCastingTime> DEFAULT_SPELL_CASTING_TIME_FILTERS = List.of(
            SpellCastingTime.of(null, CastingUnit.BONUS),
            SpellCastingTime.of(null, CastingUnit.REACTION),
            SpellCastingTime.of(null, CastingUnit.ACTION),
            SpellCastingTime.of(1L, CastingUnit.MINUTE),
            SpellCastingTime.of(10L, CastingUnit.MINUTE),
            SpellCastingTime.of(1L, CastingUnit.HOUR),
            SpellCastingTime.of(8L, CastingUnit.HOUR),
            SpellCastingTime.of(12L, CastingUnit.HOUR),
            SpellCastingTime.of(24L, CastingUnit.HOUR));

    public static SpellCastingTimeFilterRange getDefault() {
        return new SpellCastingTimeFilterRange(
                DEFAULT_SPELL_CASTING_TIME_FILTERS.stream()
                        .map(SpellCastingTimeFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public SpellCastingTimeFilterRange(List<SpellCastingTimeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<SpellCastingTime> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(casting_time) as elem where %s)",
                getPositive().stream()
                        .map(sd -> String.format("elem  @> '{\"value\": %s, \"unit\": \"%s\"}'", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining(" or "))));
        Set<SpellCastingTime> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(casting_time) as elem where %s)",
                getNegative().stream()
                        .map(sd -> String.format("elem  @> '{\"value\": %s, \"unit\": \"%s\"}'", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining(" or ")))).not());

    }

    @Override
    public String getName() {
        return NAME;
    }

    public static class SpellCastingTimeFilterItem extends AbstractFilterItem<SpellCastingTime> {

        public SpellCastingTimeFilterItem(SpellCastingTime value) {
            super(value.toString(), value, null);
        }
    }
}
