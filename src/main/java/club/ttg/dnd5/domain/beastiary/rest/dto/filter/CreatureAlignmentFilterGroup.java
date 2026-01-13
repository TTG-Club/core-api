package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Alignment;
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
@JsonTypeName("c-alm")
public class CreatureAlignmentFilterGroup extends AbstractFilterGroup<Alignment, CreatureAlignmentFilterGroup.CreatureAlignmentFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("alignment");

    public CreatureAlignmentFilterGroup(List<CreatureAlignmentFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Alignment> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Alignment::toString).collect(Collectors.toSet()));
        Set<Alignment> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Alignment::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Мировоззрение";
    }

    public static CreatureAlignmentFilterGroup getDefault() {
        return new CreatureAlignmentFilterGroup(
                Arrays.stream(Alignment.values())
                        .map(CreatureAlignmentFilterGroup.CreatureAlignmentFilterItem::new)
                        .collect(Collectors.toList()));
    }

    @FilterRegistry
    @JsonTypeName("c-alm-i")
    public static class CreatureAlignmentFilterItem extends AbstractFilterItem<Alignment> {
        public CreatureAlignmentFilterItem(Alignment value) {
            super(value.getName(), value, null);
        }
    }
}
