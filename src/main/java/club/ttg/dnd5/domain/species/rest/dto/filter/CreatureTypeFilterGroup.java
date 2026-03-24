package club.ttg.dnd5.domain.species.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
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
@FilterRegistry
@JsonTypeName("sp-ct")
public class CreatureTypeFilterGroup extends AbstractFilterGroup<CreatureType, CreatureTypeFilterGroup.CreatureTypeFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("type");

    public CreatureTypeFilterGroup(List<CreatureTypeFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureType> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream()
                .map(CreatureType::toString)
                .collect(Collectors.toSet()));
        Set<CreatureType> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream()
                .map(CreatureType::toString)
                .collect(Collectors.toSet())));
    }

    @Override
    public String getName() {
        return "Тип существа";
    }

    public static CreatureTypeFilterGroup getDefault() {
        return new CreatureTypeFilterGroup(
                Arrays.stream(CreatureType.values())
                .map(CreatureTypeFilterItem::new)
                .collect(Collectors.toList()));
    }

    @FilterRegistry
    @JsonTypeName("sp-ct-i")
    public static class CreatureTypeFilterItem extends AbstractFilterItem<CreatureType> {
        public CreatureTypeFilterItem(CreatureType value) {
            super(value.getName(), value, null);
        }
    }
}
