package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/**
 * Абстрактный базовый класс для реализации диапазонных фильтров.
 * Наследует функциональность {@link AbstractFilterGroup} и добавляет специфику работы с диапазонами значений.
 *
 * @param <V> тип значений элементов фильтра
 * @param <I> тип элементов фильтра, должен наследоваться от {@link AbstractFilterItem}
 *
 */
@Getter
public abstract class AbstractFilterRange<V, I extends AbstractFilterItem<V>> extends AbstractFilterGroup<V, I> {

    public AbstractFilterRange(List<I> filters) {
        super(filters);
    }

    /**
     * Возвращает список элементов фильтра, составляющих диапазон.
     *
     * @return список элементов диапазонного фильтра
     * При сериализации в JSON свойство будет называться "range"
     * вместо "filters для корректного отображения на фронте"
     */
    @Override
    @JsonProperty("range")
    public List<I> getFilters() {
        return super.getFilters();
    }

    /**
     * Устанавливает список элементов фильтра, составляющих диапазон.
     *
     * @param filters новый список элементов диапазонного фильтра
     * @При десериализации из JSON
     *      свойство с именем "range" будет десериализовано в "filters"
     */
    @Override
    @JsonProperty("range")
    public void setFilters(List<I> filters) {
        super.setFilters(filters);
    }
}
