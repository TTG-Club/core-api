package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureTraits;
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
public class CreatureTraitsFilterGroup extends AbstractFilterGroup<CreatureTraits, CreatureTraitsFilterGroup.CreatureTraitsFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("traits");

    public CreatureTraitsFilterGroup(List<CreatureTraitsFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureTraits> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(CreatureTraits::toString).collect(Collectors.toSet()));
        Set<CreatureTraits> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? TRUE_EXPRESSION : PATH.notIn(negativeValues.stream().map(CreatureTraits::toString).collect(Collectors.toSet())));
    }

    @Override
    public String getName() {
        return "Умения";
    }

    public static CreatureTraitsFilterGroup getDefault() {
        return getDefault(Arrays.asList(CreatureTraits.values()));
    }

    public static CreatureTraitsFilterGroup getDefault(List<CreatureTraits> popularTraits) {
        return new CreatureTraitsFilterGroup(
                popularTraits.stream()
                        .map(CreatureTraitsFilterGroup.CreatureTraitsFilterItem::new)
                        .collect(Collectors.toList())
        );
    }

    public static class CreatureTraitsFilterItem extends AbstractFilterItem<CreatureTraits> {
        public CreatureTraitsFilterItem(CreatureTraits value) {
            super(value.getName(), value, null);
        }
    }
}
