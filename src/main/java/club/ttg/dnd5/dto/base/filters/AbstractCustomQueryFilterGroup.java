package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.BinaryOperator;

@AllArgsConstructor
@Getter
public abstract class AbstractCustomQueryFilterGroup implements Filter {

    protected List<? extends AbstractCustomQueryFilterItem> filters;

    @JsonIgnore
    public BinaryOperator<BooleanExpression> getDefaultReduceOperator(){
        return BooleanExpression::and;
    }

    @Override
    public Boolean isSingular() {
        return filters.stream().allMatch(AbstractCustomQueryFilterItem::isSingular);
    }

    @Override
    public BooleanExpression getQuery() {
        return filters.stream()
                .map(AbstractCustomQueryFilterItem::getQuery)
                .filter(q -> !q.equals(TRUE_EXPRESSION))
                .reduce(getDefaultReduceOperator())
                .orElse(TRUE_EXPRESSION);
    }
}
