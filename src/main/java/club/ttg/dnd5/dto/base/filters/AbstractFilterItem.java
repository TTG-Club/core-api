package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Абстрактный базовый класс для элементов фильтрации.
 * Хранит основную информацию об элементе фильтра: имя, значение и состояние выбора.
 *
 * @param <T> тип значения элемента фильтра
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "key")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AbstractFilterItem<T> {
    /**
     * Имя фильтра для отображения на фронте.
     */
    protected String name;

    /**
     * Значение элемента фильтра.
     * Может быть любого типа в зависимости от реализации.
     */
    protected T value;

    /**
     * Флаг, указывающий выбран ли элемент.
     * Может принимать значения:
     * null - не выбран (по умолчанию)
     * true - выбран
     * false - выбрано отрицание
     */
    protected Boolean selected;

    /**
     * Возвращает текущее состояние элемента фильтра.
     * Состояние определяется на основе значения поля selected.
     *
     * @return состояние элемента в виде перечисления State:
     *         UNCHECKED - если selected равен null
     *         POSITIVE - если selected равен true
     *         NEGATIVE - если selected равен false
     */
    @JsonIgnore
    public State getState()
    {
        if (Objects.isNull(selected))
        {
            return State.UNCHECKED;
        }
        return selected ? State.POSITIVE : State.NEGATIVE;
    }
}
