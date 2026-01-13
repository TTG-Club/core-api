package club.ttg.dnd5.domain.item.rest.dto.filter;

import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
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
@JsonTypeName("i-typ")
public class ItemTypeFilterGroup extends AbstractFilterGroup<ItemType, ItemTypeFilterGroup.FilterItem>
{
    /** jsonb-колонка в таблице item */
    private static final SimplePath<Object> PATH = Expressions.path(Object.class, "item_types");

    public ItemTypeFilterGroup(List<FilterItem> filters)
    {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery()
    {
        if (isSingular())
        {
            return TRUE_EXPRESSION;
        }

        Set<ItemType> positiveValues = getPositive();
        BooleanExpression result = TRUE_EXPRESSION;

        // AND по каждому положительному значению
        if (!CollectionUtils.isEmpty(positiveValues))
        {
            for (var itemType : positiveValues)
            {
                result = result.and(
                        Expressions.booleanTemplate("{0} @> cast({1} as jsonb)",
                                PATH,
                                "[\"" + itemType.toString() + "\"]")
                );
            }
        }

        // исключения (NOT IN) через AND
        Set<ItemType> negativeValues = getNegative();
        if (!CollectionUtils.isEmpty(negativeValues))
        {
            for (var itemType : negativeValues)
            {
                result = result.and(
                        Expressions.booleanTemplate("not ({0} @> cast({1} as jsonb))",
                                PATH,
                                "[\"" + itemType.toString() + "\"]")
                );
            }
        }
        return result;
    }

    @Override
    public String getName()
    {
        return "Категория";
    }

    public static ItemTypeFilterGroup getDefault()
    {
        return new ItemTypeFilterGroup(
                Arrays.stream(ItemType.values())
                        .map(FilterItem::new)
                        .sorted(Comparator.comparing(FilterItem::getName))
                        .collect(Collectors.toList())
        );
    }

    @JsonTypeName("i-typ-i")
    public static class FilterItem extends AbstractFilterItem<ItemType>
    {
        public FilterItem(ItemType value)
        {
            super(value.getName(), value, null);
        }
    }
}
