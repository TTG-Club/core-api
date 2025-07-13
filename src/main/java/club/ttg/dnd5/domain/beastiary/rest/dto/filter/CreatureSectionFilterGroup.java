package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureSection;
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
public class CreatureSectionFilterGroup extends AbstractFilterGroup<CreatureSection, CreatureSectionFilterGroup.CreatureSectionFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("section");

    public CreatureSectionFilterGroup(List<CreatureSectionFilterGroup.CreatureSectionFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<CreatureSection> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : PATH.in(positiveValues.stream().map(CreatureSection::toString).collect(Collectors.toSet()));
        Set<CreatureSection> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : PATH.notIn(negativeValues.stream().map(CreatureSection::toString).collect(Collectors.toSet())));

    }

    public String getName() {
        return "Место обитания";
    }

    public static CreatureSectionFilterGroup getDefault() {
        return new CreatureSectionFilterGroup(
                Arrays.stream(CreatureSection.values())
                        .map(CreatureSectionFilterGroup.CreatureSectionFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureSectionFilterItem extends AbstractFilterItem<CreatureSection> {
        public CreatureSectionFilterItem(CreatureSection section) {
            super(section.getName(), section, null);
        }
    }
}
