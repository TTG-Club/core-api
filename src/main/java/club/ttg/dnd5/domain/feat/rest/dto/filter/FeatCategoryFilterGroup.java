package club.ttg.dnd5.domain.feat.rest.dto.filter;

import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
@JsonTypeName("f-ctg")
public class FeatCategoryFilterGroup extends AbstractFilterGroup<FeatCategory, FeatCategoryFilterGroup.FeatCategoryFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("category");

    public FeatCategoryFilterGroup(List<FeatCategoryFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<FeatCategory> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(FeatCategory::toString).collect(Collectors.toSet()));
        Set<FeatCategory> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(FeatCategory::toString).collect(Collectors.toSet())));
    }

    @Override
    public String getName() {
        return "Категории";
    }

    public static FeatCategoryFilterGroup getDefault() {
        return new FeatCategoryFilterGroup(
                Arrays.stream(FeatCategory.values())
                        .map(FeatCategoryFilterItem::new)
                        .collect(Collectors.toList()));
    }

    @JsonTypeName("f-ctg-i")
    public static class FeatCategoryFilterItem extends AbstractFilterItem<FeatCategory> {
        public FeatCategoryFilterItem(FeatCategory value) {
            super(value.getName(), value, null);
        }
    }
}
