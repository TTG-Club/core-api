package club.ttg.dnd5.domain.item.model.weapon;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Часть урона оружия — единица боевого движка VTTG, общая с заклинаниями.
 *
 * <p>Вид части (урон, лечение, временные хиты) и тип урона задаются ТОЛЬКО токенами
 * внутри {@link #formula} ({@code @dmg.slashing}, {@code @heal}): отдельных полей под них
 * нет. Прежняя связка «кости + тип» ({@link Weapon#getDamage()}) осталась на месте —
 * её читают лист персонажа сайта и записи, сохранённые до частей-формул.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamagePart {
    /** Формула части, например {@code 1к8@dmg.slashing}. */
    private String formula;
    /**
     * Цель части в словаре VTTG ({@code DamagePartTarget}): {@code selected} (по
     * умолчанию), {@code self}, {@code choose}.
     */
    private String target;
    /** Применять часть, только если по цели фактически нанесён урон. */
    private Boolean requiresDamage;
    /**
     * Формула при удержании оружия двумя руками (свойство «Универсальное»).
     * Заполняется только у первой части.
     */
    private String versatileFormula;
}
