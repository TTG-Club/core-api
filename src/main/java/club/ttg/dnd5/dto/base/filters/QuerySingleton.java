package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Boolean-фильтр для URL-параметров.
 * <p>
 * Формат URL: {@code param=1} (include) / {@code param=0} (exclude).
 * Отсутствие параметра — unchecked (null).
 */
@Data
@NoArgsConstructor
public class QuerySingleton
{
    private Boolean value;

    @JsonIgnore
    public boolean isActive()
    {
        return value != null;
    }

    @JsonIgnore
    public boolean isPositive()
    {
        return Boolean.TRUE.equals(value);
    }
}
