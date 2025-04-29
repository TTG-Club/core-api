package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface Filter {
    BooleanExpression TRUE_EXPRESSION = Expressions.TRUE;

    String getName();
    @JsonIgnore
    BooleanExpression getQuery();
    @JsonIgnore
    Boolean isSingular();
}
