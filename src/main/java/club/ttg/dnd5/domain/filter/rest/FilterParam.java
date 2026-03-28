package club.ttg.dnd5.domain.filter.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Декларативная привязка поля {@link club.ttg.dnd5.dto.base.filters.AbstractQueryRequest}
 * к GET-параметру.
 * <p>
 * Тип парсинга определяется автоматически по типу поля:
 * <ul>
 *   <li>{@code QueryFilter<Enum>}  → resolveEnum</li>
 *   <li>{@code QueryFilter<Long>}  → resolveLong</li>
 *   <li>{@code QueryFilter<String>}→ resolveString</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilterParam
{
    /**
     * Имя GET-параметра. По умолчанию — имя поля.
     */
    String value() default "";

    /**
     * Enum-класс для {@code QueryFilter<Enum>}.
     * Обязателен, если generic-тип поля — enum.
     */
    @SuppressWarnings("rawtypes")
    Class enumClass() default Enum.class;
}
