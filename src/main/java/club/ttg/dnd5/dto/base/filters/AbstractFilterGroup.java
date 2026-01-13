package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Абстрактный базовый класс для групп фильтров.
 * Реализует интерфейс {@link Filter} и предоставляет общую функциональность для работы с группой фильтров.
 *
 * @param <V> тип значений фильтров в группе
 * @param <I> тип элементов фильтров, должен наследоваться от {@link AbstractFilterItem}
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractFilterGroup<V, I extends AbstractFilterItem<V>> implements Filter {

    /**
     * Список фильтров, входящих в данную группу.
     */
    protected List<I> filters = new ArrayList<>();

    /**
     * Проверяет, является ли группа фильтров вырожденной.
     * Группа считается вырожденной если:
     * - список фильтров пуст
     * - все фильтры в состоянии UNCHECKED (не выбраны)
     * - все фильтры в состоянии POSITIVE (выбраны)
     * - все фильтры в состоянии NEGATIVE (исключены)
     *
     * @return true - если группа вырождена, false - в противном случае
     */
    @Override
    public Boolean isSingular() {
        return CollectionUtils.isEmpty(filters)
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.UNCHECKED))
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.POSITIVE))
                || filters.stream().map(AbstractFilterItem::getState).allMatch(Predicate.isEqual(State.NEGATIVE));
    }

    /**
     * Возвращает набор значений положительно выбранных фильтров (состояние POSITIVE).
     *
     * @return множество значений выбранных фильтров
     */
    @JsonIgnore
    public Set<V> getPositive() {
        return filters.stream()
                .filter(i -> i.getState() == State.POSITIVE)
                .map(AbstractFilterItem::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * Возвращает набор значений отрицательно выбранных фильтров (состояние NEGATIVE).
     *
     * @return множество значений исключенных фильтров
     */
    @JsonIgnore
    public Set<V> getNegative() {
        return filters.stream()
                .filter(i -> i.getState() == State.NEGATIVE)
                .map(AbstractFilterItem::getValue)
                .collect(Collectors.toSet());
    }
}

