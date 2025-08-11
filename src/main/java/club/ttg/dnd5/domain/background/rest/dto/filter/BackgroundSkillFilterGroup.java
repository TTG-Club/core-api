package club.ttg.dnd5.domain.background.rest.dto.filter;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
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
public class BackgroundSkillFilterGroup extends AbstractFilterGroup<Skill, BackgroundSkillFilterGroup.BackgroundAbilityFilterItem> {

    public BackgroundSkillFilterGroup(List<BackgroundSkillFilterGroup.BackgroundAbilityFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Характеристики";
    }

    @Override
    public BooleanExpression getQuery() {
        Set<Skill> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate("jsonb_exists_any(background.skillProficiencies,  {0} ::text[])", Expressions.constant(positiveValues.stream()
                .map(Enum::name)
                .toArray(String[]::new)));
        Set<Skill> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues)
                ? (TRUE_EXPRESSION)
                : Expressions.booleanTemplate("jsonb_exists_any(background.skillProficiencies,  {0} ::text[]) is not true", Expressions.constant(negativeValues.stream()
                .map(Enum::name)
                .toArray(String[]::new))));
    }

    public static BackgroundSkillFilterGroup getDefault() {
        return new BackgroundSkillFilterGroup(Arrays.stream(Skill.values()).
                map(BackgroundAbilityFilterItem::new)
                .collect(Collectors.toList()));
    }

    public static class BackgroundAbilityFilterItem extends AbstractFilterItem<Skill> {
        public BackgroundAbilityFilterItem(Skill value) {
            super(value.getName(), value, null);
        }
    }
}
