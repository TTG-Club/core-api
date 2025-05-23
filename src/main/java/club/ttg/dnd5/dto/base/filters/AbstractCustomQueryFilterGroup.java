package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Абстрактный базовый класс для групп фильтров в которых каждый фильтр порождает отдельное условие.
 * Предоставляет базовую реализацию для работы с группой фильтров, поддерживающих кастомные SQL-запросы.
 */
@AllArgsConstructor
@Getter
public abstract class AbstractCustomQueryFilterGroup implements Filter {

    /**
     * Список элементов фильтра, входящих в данную группу.*/
    protected List<? extends AbstractCustomQueryFilterItem> filters;

    /**
     * Возвращает оператор по умолчанию для объединения условий фильтрации.
     * По умолчанию используется логическое И ({@code AND}).
     *
     * @return бинарный оператор для объединения выражений фильтрации
     */
    @JsonIgnore
    public BinaryOperator<BooleanExpression> getDefaultReduceOperator() {
        return BooleanExpression::and;
    }

    /**
     * Проверяет, является ли группа фильтров вырожденной.
     * Группа считается вырожденной, если все её элементы являются вырожденными.
     *
     * @return true - если все фильтры в группе вырождены, false - в противном случае
     */
    @Override
    public Boolean isSingular() {
        return filters.stream().allMatch(AbstractCustomQueryFilterItem::isSingular);
    }

    /**
     * Генерирует итоговое условие фильтрации путем комбинации условий всех элементов группы.
     * Исключает тривиальные условия ({@code TRUE_EXPRESSION}) и объединяет оставшиеся
     * с помощью оператора, возвращаемого {@link #getDefaultReduceOperator()}.
     * Если все условия тривиальны, возвращает {@code TRUE_EXPRESSION}.
     *
     * @return результирующее условие фильтрации
     */
    @Override
    public BooleanExpression getQuery() {
        return filters.stream()
                .map(AbstractCustomQueryFilterItem::getQuery)
                .filter(q -> !q.equals(TRUE_EXPRESSION))
                .reduce(getDefaultReduceOperator())
                .orElse(TRUE_EXPRESSION);
    }
}