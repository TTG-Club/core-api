package club.ttg.dnd5.domain.item.rest.dto.filter;

import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
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

        final Set<ItemType> positiveValues = getPositive();
        final Set<ItemType> negativeValues = getNegative();

        BooleanExpression result = TRUE_EXPRESSION;

        // Есть хотя бы один из позитивных типов: item_types ?| array['ARMOR','WEAPON',...]
        if (CollectionUtils.isNotEmpty(positiveValues))
        {
            result = result.and(
                    Expressions.booleanTemplate("{0} ?| array[" + toSqlTextArray(positiveValues) + "]", PATH)
            );
        }

        // Нет ни одного из негативных типов: NOT (item_types ?| array['ARMOR','WEAPON',...])
        if (CollectionUtils.isNotEmpty(negativeValues))
        {
            result = result.and(
                    Expressions.booleanTemplate("NOT ({0} ?| array[" + toSqlTextArray(negativeValues) + "])", PATH)
            );
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

    public static class FilterItem extends AbstractFilterItem<ItemType>
    {
        public FilterItem(ItemType value)
        {
            super(value.getName(), value, null);
        }
    }

    private static String toSqlTextArray(Set<ItemType> values)
    {
        return values.stream()
                .map(ItemType::name)
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(","));
    }
}
