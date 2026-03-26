package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Generic 3-state фильтр для множественных значений.
 * <ul>
 *   <li>{@code include} — значения для включения (POSITIVE)</li>
 *   <li>{@code exclude} — значения для исключения (NEGATIVE)</li>
 *   <li>Отсутствие значения в обоих множествах — UNCHECKED</li>
 * </ul>
 *
 * @param <T> тип значений фильтра (enum, Long, String, etc.)
 */
@Data
@NoArgsConstructor
public class ThreeStateFilter<T>
{
    private Set<T> include = Set.of();
    private Set<T> exclude = Set.of();

    @JsonIgnore
    public boolean isActive()
    {
        return !include.isEmpty() || !exclude.isEmpty();
    }
}
