package club.ttg.dnd5.domain.item.rest.dto.filter;

import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class ItemTypeFilterGroup extends AbstractFilterGroup<ItemType, ItemTypeFilterGroup.FilterItem> {

    private static final StringPath PATH = Expressions.stringPath("item_type");

    public ItemTypeFilterGroup(List<FilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<ItemType> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(ItemType::toString).collect(Collectors.toSet()));
        Set<ItemType> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(ItemType::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Категория";
    }

    public static ItemTypeFilterGroup getDefault() {
        return new ItemTypeFilterGroup(
                Arrays.stream(ItemType.values())
                        .map(FilterItem::new)
                        .sorted(Comparator.comparing(FilterItem::getName))
                        .collect(Collectors.toList()));
    }

    public static class FilterItem extends AbstractFilterItem<ItemType> {
        public FilterItem(ItemType value) {
            super(value.getName(), value, null);
        }
    }
}
