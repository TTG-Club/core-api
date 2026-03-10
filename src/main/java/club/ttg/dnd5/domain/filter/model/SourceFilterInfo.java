package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.domain.source.rest.dto.filter.SourceGroupFilter;
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
public class SourceFilterInfo extends FilterInfo {

    public SourceFilterInfo(List<SourceGroupFilter> groups) {
        super();
        setGroups(groups);
    }

    public List<SourceGroupFilter> getGroups() {
        return (List<SourceGroupFilter>) groups;
    }

    @JsonIgnore
    @Override
    protected BinaryOperator<BooleanExpression> getReduceOperator() {
        return BooleanExpression::or;
    }
}
