package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.Condition;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class AbstractFilteringCollection<T extends AbstractFilteringField<?>> {

    protected List<T> fields;
    protected String label;

    @JsonIgnore
    public Condition getPositiveQuery() {
        Set<?> positiveFields = getPositiveFields();
        if (CollectionUtils.isEmpty(positiveFields)) {
            return DSL.trueCondition();
        }
        return getPath().in(positiveFields);
    }

    ;

    @JsonIgnore
    public Set<Condition> getQuery() {
        return Set.of(getNegativeQuery(), getPositiveQuery());
    }

    @JsonIgnore
    public Condition getNegativeQuery() {
        Set<?> negativeFields = getNegativeFields();
        if (CollectionUtils.isEmpty(negativeFields)) {
            return DSL.trueCondition();
        }
        return getPath().notIn(negativeFields);
    }

    @JsonIgnore
    protected Set<?> getPositiveFields() {
        return fields.stream()
                .filter(f -> Objects.nonNull(f.getSelected()) && f.getSelected())
                .map(AbstractFilteringField::getField)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    protected Set<?> getNegativeFields() {
        return fields.stream()
                .filter(f -> Objects.nonNull(f.getSelected()) && !f.getSelected())
                .map(AbstractFilteringField::getField)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    protected abstract TableField<?, ?> getPath();
}
