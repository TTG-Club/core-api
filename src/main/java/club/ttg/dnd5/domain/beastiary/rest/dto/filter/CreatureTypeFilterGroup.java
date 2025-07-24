package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
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
public class CreatureTypeFilterGroup extends AbstractFilterGroup<CreatureType, CreatureTypeFilterGroup.CreatureTypeFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("types");

    public CreatureTypeFilterGroup(List<CreatureTypeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<CreatureType> positiveValues = getPositive();
        Set<CreatureType> negativeValues = getNegative();

        BooleanExpression positiveExpr;
        if (CollectionUtils.isEmpty(positiveValues)) {
            positiveExpr = TRUE_EXPRESSION;
        } else {
            List<BooleanExpression> positiveConditions = positiveValues.stream()
                    .map(val -> Expressions.booleanTemplate(
                            "({0}->'values') @> '[\""+ val.toString() + "\"]'::jsonb",
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
                            "NOT (({0}->'values') @> '[\""+ val.toString() + "\"]'::jsonb)",
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
        return "Тип";
    }

    public static CreatureTypeFilterGroup getDefault() {
        return new CreatureTypeFilterGroup(
                Arrays.stream(CreatureType.values())
                        .map(CreatureTypeFilterGroup.CreatureTypeFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureTypeFilterItem extends AbstractFilterItem<CreatureType> {
        public CreatureTypeFilterItem(CreatureType value) {
            super(value.getName(), value, null);
        }
    }
}
