package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3-state фильтр для булевых признаков (ритуал, концентрация, настройка, и т.п.).
 * <ul>
 *   <li>{@code value = true}  — включить (POSITIVE)</li>
 *   <li>{@code value = false} — исключить (NEGATIVE)</li>
 *   <li>{@code value = null}  — не выбран (UNCHECKED)</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class ThreeStateSingleton
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
