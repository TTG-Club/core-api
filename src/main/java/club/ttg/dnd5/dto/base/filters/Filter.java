package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

/**
 * Интерфейс, определяющий контракт для фильтров
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "key"
)
public interface Filter {

    /**
     * Значение по умолчанию для вырожденных фильтров
     */
    BooleanExpression TRUE_EXPRESSION = Expressions.TRUE;

    /**
     * Имя фильтра для отображения на фронте.
     */
    String getName();

    /**
     * Условие для подстановки в where в query
     */
    @JsonIgnore
    BooleanExpression getQuery();

    /**
     * Определяет, является ли фильтр вырожденным (не выбран на фронте/выбраны все варианты в комбинации).
     */
    @JsonIgnore
    Boolean isSingular();
}
