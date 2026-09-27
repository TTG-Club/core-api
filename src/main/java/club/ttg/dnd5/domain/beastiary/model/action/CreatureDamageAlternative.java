package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.model.DamagePart;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Вариант урона «или» у записи существа: «20 (4к8 + 2), или 11 (2к8 + 2), если
 * рой окровавлен». Целый набор частей урона, который при атаке ЗАМЕНЯЕТ основной
 * {@link CreatureActionEffect#getDamageParts()}, а не прибавляется к нему.
 *
 * <p>Зеркало {@code CreatureDamageAlternative} игровой системы. Условие варианта
 * пишется состоянием в его же формуле ({@code @self.status.bloodied},
 * {@code @target.status.grappled}); {@link #condition} — способ выбора.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatureDamageAlternative {
    /**
     * Способ выбора строкой словаря VTTG как есть: {@code formula} — по
     * состояниям в формуле, {@code ask} — выбирает бросающий, {@code random} —
     * случайно. Своего enum нет: словарь принадлежит системе.
     */
    private String condition;

    /** Своя подпись варианта для вопроса при броске и чата («С преимуществом»). */
    private String label;

    /** Части урона варианта — тот же формат, что у основного урона. */
    private List<DamagePart> damageParts;
}
