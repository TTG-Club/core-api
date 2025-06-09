package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.QCreature;
import club.ttg.dnd5.domain.common.dictionary.ChallengeRating;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class CrFilterGroup extends AbstractFilterGroup<Long, CrFilterGroup.CrFilterItem> {

    private static final NumberPath<Long> PATH = QCreature.creature.experience;

    public CrFilterGroup(List<CrFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Уровень опасности";
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

    public static CrFilterGroup getDefault() {
        return new CrFilterGroup(Arrays.stream(ChallengeRating.values())
                .map(i -> new CrFilterItem(i.getName(), i.getExperience()))
                .collect(Collectors.toList()));
    }

    public static class CrFilterItem extends AbstractFilterItem<Long> {
        public CrFilterItem(String name, Long value) {
            super(name, value, null);
        }
    }
}
