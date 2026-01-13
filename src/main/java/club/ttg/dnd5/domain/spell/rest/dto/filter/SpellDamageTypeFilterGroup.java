package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonTypeName("s-dmg")
public class SpellDamageTypeFilterGroup extends AbstractFilterGroup<DamageType, SpellDamageTypeFilterGroup.SpellDamageTypeFilterItem> {

    private final static Set<DamageType> DAMAGE_TYPES = Set.of(
            DamageType.FAIR,
            DamageType.COLD,
            DamageType.LIGHTNING,
            DamageType.POISON,
            DamageType.ACID,
            DamageType.THUNDER,
            DamageType.NECROTIC,
            DamageType.PSYCHIC,
            DamageType.RADIANT,
            DamageType.FORCE,
            DamageType.BLUDGEONING,
            DamageType.PIERCING,
            DamageType.SLASHING);

    public SpellDamageTypeFilterGroup(List<SpellDamageTypeFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Тип урона";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<DamageType> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate("jsonb_exists_any(spell.damage_type,  {0} ::text[])", Expressions.constant(positiveValues.stream()
                .map(Enum::name)
                .toArray(String[]::new)));
        Set<DamageType> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate("jsonb_exists_any(spell.damage_type,  {0} ::text[]) is not true", Expressions.constant(negativeValues.stream()
                .map(Enum::name)
                .toArray(String[]::new))));
    }

    public static SpellDamageTypeFilterGroup getDefault() {
        return new SpellDamageTypeFilterGroup(DAMAGE_TYPES.stream()
                .map(SpellDamageTypeFilterItem::new)
                .collect(Collectors.toList()));
    }

    @JsonTypeName("s-dmg-i")
    public static class SpellDamageTypeFilterItem extends AbstractFilterItem<DamageType> {
        public SpellDamageTypeFilterItem(DamageType value) {
            super(value.getName(), value, null);
        }
    }

}
