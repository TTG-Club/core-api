package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Часть урона/лечения — единица боевого движка VTTG. Одна и та же у всего, что
 * такие формулы пишет: заклинания, активного эффекта, действия существа.
 *
 * <p>Вид части (урон, лечение, временные ХП) и тип урона задаются токенами
 * внутри {@code formula}: {@code @dmg.<тип>}, {@code @heal}, {@code @heal.temp}.
 * Поле {@code type} — подсказка старых данных, у которых токена в формуле нет;
 * у новых записей его заменяет сама формула.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamagePart {
    /** Формула части, напр. {@code 2к6@dmg.fire}. */
    private String formula;
    /** Тип урона строкой словаря VTTG; у новых записей пуст. */
    private String type;
    /** Цель части: {@code selected} (дефолт), {@code self}, {@code choose}. */
    private String target;
    /** Применять часть, только если по цели фактически нанесён урон. */
    private Boolean requiresDamage;
}
