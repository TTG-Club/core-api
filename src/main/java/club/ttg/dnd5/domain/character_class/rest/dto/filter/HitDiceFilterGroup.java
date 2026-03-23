package club.ttg.dnd5.domain.character_class.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Dice;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static club.ttg.dnd5.domain.common.dictionary.Dice.*;

@Getter
@Setter
@FilterRegistry
@JsonTypeName("c-hd")
public class HitDiceFilterGroup extends AbstractFilterGroup<Dice, HitDiceFilterGroup.HitDiceFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("hit_dice");

    public HitDiceFilterGroup(List<HitDiceFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Dice> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream()
                .map(Dice::toString)
                .collect(Collectors.toSet()));
        Set<Dice> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream()
                .map(Dice::toString)
                .collect(Collectors.toSet())));
    }

    @Override
    public String getName() {
        return "Школа";
    }

    public static HitDiceFilterGroup getDefault() {
        return new HitDiceFilterGroup(
                Stream.of(d6, d8, d10, d12)
                .map(HitDiceFilterItem::new)
                .collect(Collectors.toList()));
    }

    @FilterRegistry
    @JsonTypeName("c-hd-i")
    public static class HitDiceFilterItem extends AbstractFilterItem<Dice> {
        public HitDiceFilterItem(Dice value) {
            super(value.getName(), value, null);
        }
    }
}
