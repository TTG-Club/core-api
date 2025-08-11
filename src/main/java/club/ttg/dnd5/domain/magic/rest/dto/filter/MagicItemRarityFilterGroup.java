package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
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
public class MagicItemRarityFilterGroup extends AbstractFilterGroup<Rarity, MagicItemRarityFilterGroup.MagicItemRarityFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("category");

    public MagicItemRarityFilterGroup(List<MagicItemRarityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Rarity> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ?
                TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Rarity::toString).collect(Collectors.toSet()));
        Set<Rarity> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ?
                (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Rarity::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Редкость";
    }

    public static MagicItemRarityFilterGroup getDefault() {
        return new MagicItemRarityFilterGroup(
                Arrays.stream(Rarity.values())
                        .map(MagicItemRarityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class MagicItemRarityFilterItem extends AbstractFilterItem<Rarity> {
        public MagicItemRarityFilterItem(Rarity value) {
            super(value.getName(), value, null);
        }
    }
}
