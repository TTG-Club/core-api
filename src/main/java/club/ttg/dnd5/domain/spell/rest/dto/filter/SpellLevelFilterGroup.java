package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
@Setter
@JsonTypeName("s-lvl")
public class SpellLevelFilterGroup extends AbstractFilterGroup<Long, SpellLevelFilterGroup.SpellLevelFilterItem> {

    private static final NumberPath<Long> PATH = QSpell.spell.level;

    public SpellLevelFilterGroup(List<SpellLevelFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Уровень";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Long> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues);
        Set<Long> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues));

    }

    public static SpellLevelFilterGroup getDefault() {
        return new SpellLevelFilterGroup(LongStream.range(0, 10).boxed()
                .map(i -> new SpellLevelFilterItem(i == 0L ? "заговор" : i.toString(), i))
                .collect(Collectors.toList()));
    }

    @JsonTypeName("s-lvl-i")
    public static class SpellLevelFilterItem extends AbstractFilterItem<Long> {
        public SpellLevelFilterItem(String name, Long value) {
            super(name, value, null);
        }
    }
}
