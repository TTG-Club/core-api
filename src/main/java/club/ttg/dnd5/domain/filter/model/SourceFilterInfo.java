package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.dto.base.filters.Filter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.BinaryOperator;

@Getter
@Setter
@NoArgsConstructor
public final class SourceFilterInfo extends FilterInfo {

    public SourceFilterInfo(List<Filter> groups) {
        super();
        setGroups(groups);
    }

    @JsonIgnore
    @Override
    protected BinaryOperator<BooleanExpression> getReduceOperator() {
        return BooleanExpression::or;
    }
}
