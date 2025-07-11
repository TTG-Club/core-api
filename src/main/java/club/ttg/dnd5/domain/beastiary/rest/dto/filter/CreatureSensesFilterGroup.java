package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureSenses;
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
public class CreatureSensesFilterGroup extends AbstractFilterGroup<CreatureSenses, CreatureSensesFilterGroup.CreatureSensesFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("senses");

    public CreatureSensesFilterGroup(List<CreatureSensesFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureSenses> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(CreatureSenses::toString).collect(Collectors.toSet()));
        Set<CreatureSenses> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(CreatureSenses::toString).collect(Collectors.toSet())));

    }

    public String getName() {
        return "Чувство";
    }

    public static CreatureSensesFilterGroup getDefault() {
        return new CreatureSensesFilterGroup(
                Arrays.stream(CreatureSenses.values())
                        .map(CreatureSensesFilterGroup.CreatureSensesFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureSensesFilterItem extends AbstractFilterItem<CreatureSenses> {
        public CreatureSensesFilterItem(CreatureSenses senses) {
            super(senses.getName(), senses, null);
        }
    }
}
