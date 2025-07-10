package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureTypes;
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
public class CreatureTypesFilterGroup extends AbstractFilterGroup<CreatureTypes, CreatureTypesFilterGroup.CreatureTypesFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("types");

    public CreatureTypesFilterGroup(List<CreatureTypesFilterGroup.CreatureTypesFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureTypes> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(CreatureTypes::toString).collect(Collectors.toSet()));
        Set<CreatureTypes> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(CreatureTypes::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Тип";
    }

    public static CreatureTypesFilterGroup getDefault() {
        return new CreatureTypesFilterGroup(
                Arrays.stream(CreatureTypes.values())
                        .map(CreatureTypesFilterGroup.CreatureTypesFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureTypesFilterItem extends AbstractFilterItem<CreatureTypes> {
        public CreatureTypesFilterItem(CreatureTypes value) {
            super(value.getName(), value, null);
        }
    }
}
