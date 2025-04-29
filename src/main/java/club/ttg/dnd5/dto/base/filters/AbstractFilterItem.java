package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
@AllArgsConstructor
@Getter
@Setter
public class AbstractFilterItem<T> {
    protected String name;
    protected T value;
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



}
