package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CreatureSectionFilterGroup extends AbstractFilterGroup<Habitat, CreatureSectionFilterGroup.CreatureSectionFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("section");

    public CreatureSectionFilterGroup(List<CreatureSectionFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Habitat> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Habitat::toString).collect(Collectors.toSet()));
        Set<Habitat> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Habitat::toString).collect(Collectors.toSet())));

    }

    public String getName() {
        return "Место обитания";
    }

    public static CreatureSectionFilterGroup getDefault() {
        return new CreatureSectionFilterGroup(
                Arrays.stream(Habitat.values())
                        .map(CreatureSectionFilterGroup.CreatureSectionFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureSectionFilterItem extends AbstractFilterItem<Habitat> {
        public CreatureSectionFilterItem(Habitat values) {
            super(values.getName(), values, null);
        }
    }
}
