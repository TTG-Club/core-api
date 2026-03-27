package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Универсальный фильтр для URL-параметров.
 * <p>
 * Формат URL: {@code param=a,b,c&param_mode=1&param_union=1}
 * <ul>
 *   <li>{@code values} — выбранные значения из {@code param=a,b,c}</li>
 *   <li>{@code exclude} — режим исключения из {@code param_mode=1} (по умолчанию include)</li>
 *   <li>{@code union} — логика объединения из {@code param_union=1} (по умолчанию AND)</li>
 * </ul>
 *
 * @param <T> тип значений фильтра (enum, Long, String и т.д.)
 */
@Data
@NoArgsConstructor
public class QueryFilter<T>
{
    private Set<T> values = Set.of();
    private boolean exclude = false;
    private boolean union = false;

    @JsonIgnore
    public boolean isActive()
    {
        return !values.isEmpty();
    }
}
