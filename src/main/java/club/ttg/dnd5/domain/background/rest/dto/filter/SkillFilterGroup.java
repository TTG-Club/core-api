package club.ttg.dnd5.domain.background.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import club.ttg.dnd5.dto.base.filters.FilterRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
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
@FilterRegistry
@JsonTypeName("b-skl")
public class SkillFilterGroup extends AbstractFilterGroup<Skill, SkillFilterGroup.AbilityFilterItem> {
    public SkillFilterGroup(List<AbilityFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<Skill> positiveValues = getPositive();
        BooleanExpression result = TRUE_EXPRESSION;
        if (CollectionUtils.isNotEmpty(positiveValues)) {
            String jsonArray = positiveValues.stream()
                    .map(Skill::toString)
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
            result = Expressions.booleanTemplate(
                    "skill_proficiencies @> cast({0} as jsonb)", jsonArray
            );
        }
        // отрицательные — чуть сложнее, можно через not @>
        Set<Skill> negativeValues = getNegative();
        if (CollectionUtils.isNotEmpty(negativeValues)) {
            for (Skill neg : negativeValues) {
                String json = "[\"" + neg.toString() + "\"]";
                result = result.and(Expressions.booleanTemplate(
                        "not (skill_proficiencies @> cast({0} as jsonb))", json
                ));
            }
        }
        return result;
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

    @FilterRegistry
    @JsonTypeName("b-skl-i")
    public static class AbilityFilterItem extends AbstractFilterItem<Skill> {
        public AbilityFilterItem(Skill value) {
            super(value.getName(), value, null);
        }
    }
}
