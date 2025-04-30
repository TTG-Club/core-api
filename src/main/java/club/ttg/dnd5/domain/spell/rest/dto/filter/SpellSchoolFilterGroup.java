package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class SpellSchoolFilterGroup extends AbstractFilterGroup<MagicSchool, SpellSchoolFilterGroup.SpellSchoolFilterItem> {

    private static final  EnumPath<MagicSchool> PATH = Expressions.enumPath(MagicSchool.class, "school");

    public SpellSchoolFilterGroup(List<SpellSchoolFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<MagicSchool> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues);
        Set<MagicSchool> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues));

    }

    @Override
    public String getName() {
        return "Школа";
    }

    public static SpellSchoolFilterGroup getDefault() {
        return new SpellSchoolFilterGroup(
                Arrays.stream(MagicSchool.values())
                        .map(SpellSchoolFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class SpellSchoolFilterItem extends AbstractFilterItem<MagicSchool> {
        public SpellSchoolFilterItem(MagicSchool value) {
            super(value.getName(), value, null);
        }
    }


}
