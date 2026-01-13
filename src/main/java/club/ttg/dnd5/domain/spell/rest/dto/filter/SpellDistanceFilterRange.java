package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonTypeName("s-dis")
public class SpellDistanceFilterRange extends AbstractFilterGroup<SpellDistance, SpellDistanceFilterRange.SpellCastingTimeFilterItem> {

    private static final String NAME = "Дистанция";
    private static final List<SpellDistance> DEFAULT_SPELL_DISTANCE_FILTERS = List.of(
            SpellDistance.of(null, DistanceUnit.SELF),
            SpellDistance.of(null, DistanceUnit.TOUCH),
            SpellDistance.of(5L, DistanceUnit.FEET),
            SpellDistance.of(10L, DistanceUnit.FEET),
            SpellDistance.of(20L, DistanceUnit.FEET),
            SpellDistance.of(25L, DistanceUnit.FEET),
            SpellDistance.of(30L, DistanceUnit.FEET),
            SpellDistance.of(40L, DistanceUnit.FEET),
            SpellDistance.of(50L, DistanceUnit.FEET),
            SpellDistance.of(60L, DistanceUnit.FEET),
            SpellDistance.of(90L, DistanceUnit.FEET),
            SpellDistance.of(100L, DistanceUnit.FEET),
            SpellDistance.of(5L, DistanceUnit.FEET),
            SpellDistance.of(120L, DistanceUnit.FEET),
            SpellDistance.of(150L, DistanceUnit.FEET),
            SpellDistance.of(300L, DistanceUnit.FEET),
            SpellDistance.of(400L, DistanceUnit.FEET),
            SpellDistance.of(1000L, DistanceUnit.FEET),
            SpellDistance.of(1L, DistanceUnit.MILE),
            SpellDistance.of(500L, DistanceUnit.MILE));

    public static SpellDistanceFilterRange getDefault() {
        return new SpellDistanceFilterRange(
                DEFAULT_SPELL_DISTANCE_FILTERS.stream()
                        .map(SpellCastingTimeFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public SpellDistanceFilterRange(List<SpellCastingTimeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<SpellDistance> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(range) as elem where %s)",
                getPositive().stream()
                        .map(sd -> String.format("elem  @> '{\"value\": %s, \"unit\": \"%s\"}'", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining(" or "))));
        Set<SpellDistance> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(range) as elem where %s)",
                getNegative().stream()
                        .map(sd -> String.format("elem  @> '{\"value\": %s, \"unit\": \"%s\"}'", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining(" or ")))).not());

    }

    @Override
    public String getName() {
        return NAME;
    }

    @JsonTypeName("s-dis-i")
    public static class SpellCastingTimeFilterItem extends AbstractFilterItem<SpellDistance> {

        public SpellCastingTimeFilterItem(SpellDistance value) {
            super(value.toString(), value, null);
        }
    }
}
