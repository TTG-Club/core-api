package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Size;
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
public class CreatureSizeFilterGroup extends AbstractFilterGroup<Size, CreatureSizeFilterGroup.SizeFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("sizes");

    public CreatureSizeFilterGroup(List<SizeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<Size> positiveValues = getPositive();
        Set<Size> negativeValues = getNegative();

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
        return "Размер";
    }

    public static CreatureSizeFilterGroup getDefault() {
        return new CreatureSizeFilterGroup(
                Arrays.stream(Size.values())
                        .map(CreatureSizeFilterGroup.SizeFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class SizeFilterItem extends AbstractFilterItem<Size> {
        public SizeFilterItem(Size value) {
            super(value.getName(), value, null);
        }
    }
}
