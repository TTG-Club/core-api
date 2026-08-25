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
    /**
     * Применять часть, только если по цели фактически нанесён урон ({@code > 0}).
     * Не задано — часть применяется независимо.
     */
    private Boolean requiresDamage;
    /**
     * Формула при удержании оружия двумя руками (свойство «Универсальное»). Заклинания
     * и существа поле игнорируют; у оружия оно заполняется только у первой части.
     */
    private String versatileFormula;
}
