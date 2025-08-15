package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class CreatureHabittatFilterGroup extends AbstractFilterGroup<Habitat, CreatureHabittatFilterGroup.CreatureHabittatFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("section");

    public CreatureHabittatFilterGroup(List<CreatureHabittatFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<Habitat> positiveValues = getPositive();
        Set<Habitat> negativeValues = getNegative();

        BooleanExpression positiveExpr;
        if (CollectionUtils.isEmpty(positiveValues)) {
            positiveExpr = TRUE_EXPRESSION;
        } else {
            List<BooleanExpression> positiveConditions = positiveValues.stream()
                    .map(val -> Expressions.booleanTemplate(
                            "({0}->'habitats') @> '[\""+ val.toString() + "\"]'::jsonb",
                            PATH))
                    .collect(Collectors.toList());

            positiveExpr = positiveConditions.stream()
                    .reduce(BooleanExpression::or)
                    .orElse(TRUE_EXPRESSION);
        }

        BooleanExpression negativeExpr;
        if (CollectionUtils.isEmpty(negativeValues)) {
            negativeExpr = TRUE_EXPRESSION;
        } else {
            List<BooleanExpression> negativeConditions = negativeValues.stream()
                    .map(val -> Expressions.booleanTemplate(
                            "NOT (({0}->'habitats') @> '[\""+ val.toString() + "\"]'::jsonb)",
                            PATH))
                    .collect(Collectors.toList());

            negativeExpr = negativeConditions.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(TRUE_EXPRESSION);
        }

        return positiveExpr.and(negativeExpr);
    }

    @Override
    public String getName() {
        return "Место обитания";
    }

    public static CreatureHabittatFilterGroup getDefault() {
        return new CreatureHabittatFilterGroup(
                Arrays.stream(Habitat.values())
                        .map(CreatureHabittatFilterGroup.CreatureHabittatFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureHabittatFilterItem extends AbstractFilterItem<Habitat> {
        public CreatureHabittatFilterItem(Habitat value) {
            super(value.getName(), value, null);
        }
    }
}
