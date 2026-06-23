package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Часть урона/лечения в формате компендиума VTTG ({@code DamagePart}).
 *
 * <p>Вид части (урон/лечение/временные ХП) и тип урона задаются ТОЛЬКО токенами
 * в строке {@code formula}: {@code @dmg.<type>}, {@code @heal}, {@code @heal.temp}
 * (см. COMBAT.md). Легаси-флаг {@code isHealing} удалён.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgDamagePart {
    private String formula;
    private String type;
    private String target;
}
