package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.dto.base.filters.Filter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static club.ttg.dnd5.dto.base.filters.Filter.TRUE_EXPRESSION;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FilterInfo {
    List<Filter> groups;
    String version;
    @JsonIgnore
    public BooleanExpression getQuery() {
        return groups.stream().map(Filter::getQuery)
                .filter(q -> !q.equals(TRUE_EXPRESSION))
                .reduce(BooleanExpression::and)
                .orElse(TRUE_EXPRESSION);
    }
}
