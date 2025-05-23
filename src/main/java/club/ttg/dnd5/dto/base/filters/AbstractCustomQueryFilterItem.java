package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Абстрактный базовый класс для элементов фильтра.
 * Предоставляет базовую реализацию для фильтров, поддерживающих кастомную логику формирования SQL-запросов.
 */
@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractCustomQueryFilterItem implements Filter {

    /**
     * Наименование элемента фильтра.
     * Используется для отображения на фронте.
     */
    protected String name;

    /**
     * Флаг выбора элемента фильтра.
     * Может принимать значения:
     * null - не выбран (UNCHECKED)
     * true - выбран (POSITIVE)
     * false - исключен (NEGATIVE)
     */
    protected Boolean selected;

    /**
     * Возвращает текущее состояние элемента фильтра.
     *
     * @return состояние элемента {@link State}:
     *         UNCHECKED - если selected равен null,
     *         POSITIVE - если selected равен true,
     *         NEGATIVE - если selected равен false
     */
    @JsonIgnore
    public State getState() {
        if (Objects.isNull(selected)) {
            return State.UNCHECKED;
        }
        return selected ? State.POSITIVE : State.NEGATIVE;
    }

    /**
     * Возвращает SQL-условие для фильтрации на основе текущего состояния.
     * Формирует запрос в зависимости от состояния элемента:
     * POSITIVE - использует {@link #getPositiveQuery()}
     * NEGATIVE - использует {@link #getNegativeQuery()}
     * UNCHECKED - возвращает тривиальное условие {@code TRUE_EXPRESSION}
     *
     * @return сформированное условие фильтрации
     */
    @Override
    public BooleanExpression getQuery() {
        return switch (getState()) {
            case POSITIVE -> getPositiveQuery();
            case NEGATIVE -> getNegativeQuery();
            case UNCHECKED -> TRUE_EXPRESSION;
        };
    }

    /**
     * Абстрактный метод для получения SQL-условия при положительном выборе (POSITIVE).
     * Должен быть реализован в наследниках.
     *
     * @return условие фильтрации для положительного выбора
     */
    @JsonIgnore
    public abstract BooleanExpression getPositiveQuery();

    /**
     * Абстрактный метод для получения SQL-условия при отрицательном выборе (NEGATIVE).
     * Должен быть реализован в наследниках.
     *
     * @return условие фильтрации для отрицательного выбора
     */
    @JsonIgnore
    public abstract BooleanExpression getNegativeQuery();

    /**
     * Проверяет, является ли фильтр вырожденным.
     * Фильтр считается вырожденным, если находится в состоянии UNCHECKED.
     *
     * @return true - если фильтр в состоянии UNCHECKED, false - в противном случае
     */
    @Override
    public Boolean isSingular() {
        return getState() == State.UNCHECKED;
    }
}
