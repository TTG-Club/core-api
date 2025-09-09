package club.ttg.dnd5.domain.background.rest.dto.filter;

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
public class AbilityFilterGroup extends AbstractFilterGroup<Ability, AbilityFilterGroup.AbilityFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("abilities");

    public AbilityFilterGroup(List<AbilityFilterItem> filters) {
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
        return "Характеристики";
    }

    public static AbilityFilterGroup getDefault() {
        return new AbilityFilterGroup(
                Arrays.stream(Ability.values())
                        .map(AbilityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class AbilityFilterItem extends AbstractFilterItem<Ability> {
        public AbilityFilterItem(Ability value) {
            super(value.getName(), value, null);
        }
    }
}
