package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractCustomQueryFilterItem implements Filter {

    protected String name;
    protected Boolean selected;


    @JsonIgnore
    public State getState() {
        if (Objects.isNull(selected)) {
            return State.UNCHECKED;
        }
        if (selected) {
            return State.POSITIVE;
        }
        return State.NEGATIVE;
    }

    @Override
    public BooleanExpression getQuery() {
        return switch (getState()) {
            case POSITIVE -> getPositiveQuery();
            case NEGATIVE -> getNegativeQuery();
            case UNCHECKED -> TRUE_EXPRESSION;
        };
    }

    @JsonIgnore
    public abstract BooleanExpression getPositiveQuery();

    @JsonIgnore
    public abstract BooleanExpression getNegativeQuery();

    @Override
    public Boolean isSingular() {
        return getState() == State.UNCHECKED;
    }
}
