package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "key"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SourceGroupFilter.class, name = "src"),
        @JsonSubTypes.Type(value = SourceGroupFilter.SourceFilterItem.class, name = "src-i")
})
public interface Filter
{
    BooleanExpression TRUE_EXPRESSION = Expressions.TRUE;

    String getName();

    @JsonIgnore
    BooleanExpression getQuery();

    @JsonIgnore
    Boolean isSingular();
}