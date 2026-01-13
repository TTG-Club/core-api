package club.ttg.dnd5.domain.magic.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
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
@JsonTypeName("mi-rar")
public class RarityFilterGroup extends AbstractFilterGroup<Rarity, RarityFilterGroup.RarityFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("rarity");

    public RarityFilterGroup(List<RarityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Rarity> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Rarity::toString).collect(Collectors.toSet()));
        Set<Rarity> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Rarity::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Редкость";
    }

    public static RarityFilterGroup getDefault() {
        return new RarityFilterGroup(
                Arrays.stream(Rarity.values())
                        .map(RarityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    @JsonTypeName("mi-rar-i")
    public static class RarityFilterItem extends AbstractFilterItem<Rarity> {
        public RarityFilterItem(Rarity value) {
            super(value.getName(), value, null);
        }
    }
}
