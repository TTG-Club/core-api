package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
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
public class MagicItemCategoryFilterGroup extends AbstractFilterGroup<MagicItemCategory, MagicItemCategoryFilterGroup.MagicItemCategoryFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("category");

    public MagicItemCategoryFilterGroup(List<MagicItemCategoryFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<MagicItemCategory> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ?
                TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(MagicItemCategory::toString).collect(Collectors.toSet()));
        Set<MagicItemCategory> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ?
                (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(MagicItemCategory::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Категория";
    }

    public static MagicItemCategoryFilterGroup getDefault() {
        return new MagicItemCategoryFilterGroup(
                Arrays.stream(MagicItemCategory.values())
                        .map(MagicItemCategoryFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class MagicItemCategoryFilterItem extends AbstractFilterItem<MagicItemCategory> {
        public MagicItemCategoryFilterItem(MagicItemCategory value) {
            super(value.getName(), value, null);
        }
    }
}
