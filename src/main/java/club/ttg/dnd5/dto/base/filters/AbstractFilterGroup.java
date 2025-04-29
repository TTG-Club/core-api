package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractFilterGroup<V, I extends AbstractFilterItem<V>> implements Filter {

    protected List<I> filters;

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }
        Set<V> positiveValues = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positiveValues) ? TRUE_EXPRESSION : getPATH().in(positiveValues);
        Set<V> negativeValues = getNegative();
        return result.and(CollectionUtils.isEmpty(negativeValues) ? (TRUE_EXPRESSION) : getPATH().notIn(negativeValues));

   }


    @Override
    public Boolean isSingular() {
        return CollectionUtils.isEmpty(filters)
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.UNCHECKED))
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.POSITIVE))
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.NEGATIVE));
    }

    @JsonIgnore
    public Set<V> getPositive() {
        return filters.stream()
                .filter(i -> i.getState() == State.POSITIVE)
                .map(AbstractFilterItem::getValue)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<V> getNegative() {
        return filters.stream()
                .filter(i -> i.getState() == State.NEGATIVE)
                .map(AbstractFilterItem::getValue)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    protected SimpleExpression<V> getPATH(){
        return null;
    }
}
