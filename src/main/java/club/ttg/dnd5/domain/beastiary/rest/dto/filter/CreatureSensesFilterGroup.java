package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonTypeName("c-sen")
public class CreatureSensesFilterGroup extends AbstractFilterGroup<CreatureSenses, CreatureSensesFilterGroup.CreatureSensesFilterItem> {

    public CreatureSensesFilterGroup(List<CreatureSensesFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        Set<CreatureSenses> positiveValues = getPositive();
        Set<CreatureSenses> negativeValues = getNegative();

        List<BooleanExpression> positiveExpressions = positiveValues.stream()
                .map(sense -> {
                    String key = sense.toString().toLowerCase();

                    if ("unimpeded".equals(key)) {
                        return Expressions.booleanTemplate(
                                "(senses ->> {0}) = 'true'",
                                key
                        );
                    }

                    return Expressions.booleanTemplate(
                            "(senses ->> {0}) ~ '^\\d+$' AND (senses ->> {0})::int > 0",
                            key
                    );
                })
                .collect(Collectors.toList());

        List<BooleanExpression> negativeExpressions = negativeValues.stream()
                .map(sense -> {
                    String key = sense.toString().toLowerCase();

                    if ("unimpeded".equals(key)) {
                        return Expressions.booleanTemplate(
                                "((senses ->> {0}) IS NULL OR (senses ->> {0}) != 'true')",
                                key
                        );
                    }

                    return Expressions.booleanTemplate(
                            "((senses ->> {0}) IS NULL OR (senses ->> {0}) !~ '^\\d+$' OR (senses ->> {0})::int <= 0)",
                            key
                    );
                })
                .collect(Collectors.toList());

        BooleanExpression positiveCombined = positiveExpressions.stream()
                .reduce(BooleanExpression::or)
                .orElse(TRUE_EXPRESSION);

        BooleanExpression negativeCombined = negativeExpressions.stream()
                .reduce(BooleanExpression::and)
                .orElse(TRUE_EXPRESSION);

        return positiveCombined.and(negativeCombined);
    }

    @Override
    public String getName() {
        return "Чувство";
    }

    public static CreatureSensesFilterGroup getDefault() {
        return new CreatureSensesFilterGroup(
                Arrays.stream(CreatureSenses.values())
                        .map(CreatureSensesFilterGroup.CreatureSensesFilterItem::new)
                        .collect(Collectors.toList()));
    }

    @JsonTypeName("c-sen-i")
    public static class CreatureSensesFilterItem extends AbstractFilterItem<CreatureSenses> {
        public CreatureSensesFilterItem(CreatureSenses value) {
            super(value.getName(), value, null);
        }
    }
}
