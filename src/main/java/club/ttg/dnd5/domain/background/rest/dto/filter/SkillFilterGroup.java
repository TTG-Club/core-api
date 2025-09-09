package club.ttg.dnd5.domain.background.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Skill;
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
public class SkillFilterGroup extends AbstractFilterGroup<Skill, SkillFilterGroup.AbilityFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("skillProficiencies");

    public SkillFilterGroup(List<AbilityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Skill> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(Skill::toString).collect(Collectors.toSet()));
        Set<Skill> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(Skill::toString).collect(Collectors.toSet())));

    }

    @Override
    public String getName() {
        return "Навыки";
    }

    public static SkillFilterGroup getDefault() {
        return new SkillFilterGroup(
                Arrays.stream(Skill.values())
                        .map(AbilityFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class AbilityFilterItem extends AbstractFilterItem<Skill> {
        public AbilityFilterItem(Skill value) {
            super(value.getName(), value, null);
        }
    }
}
