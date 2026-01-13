package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonTypeName("s-dur")
public class SpellDurationFilterRange extends AbstractFilterGroup<SpellDuration, SpellDurationFilterRange.SpellDurationFilterItem> {

    private static final String NAME = "Длительность";
    private static final List<SpellDuration> DEFAULT_SPELL_DURATION_FILTERS = List.of(
            SpellDuration.of(null, DurationUnit.INSTANT),
            SpellDuration.of(null, DurationUnit.ROUND),
            SpellDuration.of(1L, DurationUnit.MINUTE),
            SpellDuration.of(10L, DurationUnit.MINUTE),
            SpellDuration.of(1L, DurationUnit.HOUR),
            SpellDuration.of(8L, DurationUnit.HOUR),
            SpellDuration.of(12L, DurationUnit.HOUR),
            SpellDuration.of(24L, DurationUnit.HOUR),
            SpellDuration.of(1L, DurationUnit.DAY),
            SpellDuration.of(7L, DurationUnit.DAY),
            SpellDuration.of(10L, DurationUnit.DAY),
            SpellDuration.of(1L, DurationUnit.YEAR),
            SpellDuration.of(null, DurationUnit.UNTIL_DISPEL),
            SpellDuration.of(null, DurationUnit.PERMANENT));

    public static SpellDurationFilterRange getDefault() {
        return new SpellDurationFilterRange(
                DEFAULT_SPELL_DURATION_FILTERS.stream()
                        .map(SpellDurationFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public SpellDurationFilterRange(List<SpellDurationFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<SpellDuration> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(duration) as elem where %s)",
                getPositive().stream()
                        .map(sd -> String.format("""
                                elem  @> '{"value": %s, "unit":"%s"}'""", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining("or"))));
        Set<SpellDuration> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate(String.format("exists (select 1 from jsonb_array_elements(duration) as elem where %s)",
                getNegative().stream()
                        .map(sd -> String.format("""
                                elem  @> '{"value": %s, "unit": "%s"}'""", sd.getValue(), sd.getUnit()))
                        .collect(Collectors.joining("or")))).not());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @JsonTypeName("s-dur-i")
    public static class SpellDurationFilterItem extends AbstractFilterItem<SpellDuration> {

        public SpellDurationFilterItem(SpellDuration value) {
            super(value.toString(), value, null);
        }
    }
}
