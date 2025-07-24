package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.AlignmentFilter;
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
public class CreatureAlignmentFilterGroup extends AbstractFilterGroup<AlignmentFilter, CreatureAlignmentFilterGroup.CreatureAlignmentFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("alignment");

    public CreatureAlignmentFilterGroup(List<CreatureAlignmentFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<AlignmentFilter> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(AlignmentFilter::toString).collect(Collectors.toSet()));
        Set<AlignmentFilter> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(AlignmentFilter::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Мировоззрение";
    }

    public static CreatureAlignmentFilterGroup getDefault() {
        return new CreatureAlignmentFilterGroup(
                Arrays.stream(AlignmentFilter.values())
                        .map(CreatureAlignmentFilterGroup.CreatureAlignmentFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureAlignmentFilterItem extends AbstractFilterItem<AlignmentFilter> {
        public CreatureAlignmentFilterItem(AlignmentFilter value) {
            super(value.getName(), value, null);
        }
    }
}
