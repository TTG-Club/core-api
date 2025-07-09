package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureSize;
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
public class CreatureSizeFilterGroup extends AbstractFilterGroup<CreatureSize, CreatureSizeFilterGroup.SizeFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("size");

    public CreatureSizeFilterGroup(List<SizeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureSize> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(CreatureSize::toString).collect(Collectors.toSet()));
        Set<CreatureSize> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(CreatureSize::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Размер";
    }

    public static CreatureSizeFilterGroup getDefault() {
        return new CreatureSizeFilterGroup(
                Arrays.stream(CreatureSize.values())
                        .map(CreatureSizeFilterGroup.SizeFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class SizeFilterItem extends AbstractFilterItem<CreatureSize> {
        public SizeFilterItem(CreatureSize value) {
            super(value.getName(), value, null);
        }
    }
}
