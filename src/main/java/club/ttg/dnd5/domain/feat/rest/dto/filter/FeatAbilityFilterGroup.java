package club.ttg.dnd5.domain.feat.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Ability;
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
public class FeatAbilityFilterGroup extends AbstractFilterGroup<Ability, FeatAbilityFilterGroup.FeatAbilityFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("abilities");

    public FeatAbilityFilterGroup(List<FeatAbilityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Ability> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Ability::toString).collect(Collectors.toSet()));
        Set<Ability> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Ability::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Улучшаемые характеристики";
    }

    public static FeatAbilityFilterGroup getDefault() {
        return new FeatAbilityFilterGroup(
                Arrays.stream(Ability.values())
                        .map(FeatAbilityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class FeatAbilityFilterItem extends AbstractFilterItem<Ability> {
        public FeatAbilityFilterItem(Ability value) {
            super(value.getName(), value, null);
        }
    }
}
