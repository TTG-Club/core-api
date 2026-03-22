package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.dto.base.filters.Filter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import static club.ttg.dnd5.dto.base.filters.Filter.TRUE_EXPRESSION;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilterInfo
{
    protected List<Filter> groups = new ArrayList<>();

    @JsonIgnore
    protected BinaryOperator<BooleanExpression> getReduceOperator(){
        return BooleanExpression::and;
    }

    @JsonIgnore
    public BooleanExpression getQuery()
    {
        return groups.stream()
                .map(Filter::getQuery)
                .filter(q -> !q.equals(TRUE_EXPRESSION))
                .reduce(getReduceOperator())
                .orElse(TRUE_EXPRESSION);
    }
}
