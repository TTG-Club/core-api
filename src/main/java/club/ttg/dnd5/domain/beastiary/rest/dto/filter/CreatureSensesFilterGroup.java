package club.ttg.dnd5.domain.beastiary.rest.dto.filter;

import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureSenses;
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
public class CreatureSensesFilterGroup extends AbstractFilterGroup<CreatureSenses, CreatureSensesFilterGroup.CreatureSensesFilterItem> {

    private static final StringPath PATH = Expressions.stringPath("senses");

    public CreatureSensesFilterGroup(List<CreatureSensesFilterItem> filters) {
        super(filters);
    }

    @Override
    public BooleanExpression getQuery() {
        Set<CreatureSenses> positiveValues = getPositive();
        if (CollectionUtils.isEmpty(positiveValues)) {
            return TRUE_EXPRESSION;
        }

        // Создаем выражения по каждому нужному ключу, проверяем что значение не null и > 0
        List<BooleanExpression> expressions = positiveValues.stream()
                .map(sense -> Expressions.booleanTemplate(
                        "(senses ->> {0} IS NOT NULL AND (senses ->> {0})::int > 0)",
                        sense.toString().toLowerCase() // ключ в JSON у тебя в нижнем регистре
                ))
                .collect(Collectors.toList());

        // Объединяем все условия ИЛИ, т.е. достаточно, чтобы хотя бы один был true
        BooleanExpression combined = expressions.stream()
                .reduce(BooleanExpression::or)
                .orElse(TRUE_EXPRESSION);

        return combined;
    }

    @Override
    public String getName() {
        return "Чувство";
    }

    public static CreatureSensesFilterGroup getDefault() {
        return new CreatureSensesFilterGroup(
                Arrays.stream(CreatureSenses.values())
                        .map(CreatureSensesFilterGroup.CreatureSensesFilterItem::new)
                        .collect(Collectors.toList()));
    }

    public static class CreatureSensesFilterItem extends AbstractFilterItem<CreatureSenses> {
        public CreatureSensesFilterItem(CreatureSenses value) {
            super(value.getName(), value, null);
        }
    }
}
