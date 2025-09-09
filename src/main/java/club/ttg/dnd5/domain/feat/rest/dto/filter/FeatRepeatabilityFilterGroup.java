package club.ttg.dnd5.domain.feat.rest.dto.filter;

import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

public class FeatRepeatabilityFilterGroup
        extends AbstractFilterGroup<Boolean, FeatRepeatabilityFilterGroup.FeatRepeatabilityFilterItem> {

    private static final BooleanPath PATH = QFeat.feat.repeatability;

    public FeatRepeatabilityFilterGroup(List<FeatRepeatabilityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        Set<Boolean> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : PATH.in(positiveValues);

        Set<Boolean> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? TRUE_EXPRESSION
                : PATH.notIn(negativeValues));
    }

    @Override
    public String getName() {
        return "Повторяемость";
    }

    public static FeatRepeatabilityFilterGroup getDefault() {
        return new FeatRepeatabilityFilterGroup(
                List.of(
                        new FeatRepeatabilityFilterItem("есть", true),
                        new FeatRepeatabilityFilterItem("нет", false)
                )
        );
    }

    public static class FeatRepeatabilityFilterItem extends AbstractFilterItem<Boolean> {
        public FeatRepeatabilityFilterItem(String name, Boolean value) {
            super(name, value, null);
        }
    }
}
