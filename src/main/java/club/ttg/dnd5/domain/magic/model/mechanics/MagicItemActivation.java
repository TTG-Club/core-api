package club.ttg.dnd5.domain.magic.model.mechanics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Условие, при котором механика предмета работает.
 *
 * <p>Лист персонажа применяет эффекты, только когда состояние предмета в инвентаре
 * совпадает с условием: {@link #WORN} и {@link #HELD} требуют отметки «надет»,
 * {@link #EQUIPPED} — «экипирован» (оружие в руках, доспех на теле),
 * {@link #CARRIED} достаточно наличия в инвентаре. Требование настройки берётся
 * отдельно из {@code attunement} и проверяется дополнительно.</p>
 *
 * <p>Своего аналога в VTTG у этого различения нет — там эффекты предмета
 * включает один признак «экипирован», поэтому в экспорт условие не идёт.</p>
 */
@Getter
@AllArgsConstructor
public enum MagicItemActivation {
    CARRIED("при себе"),
    WORN("надет"),
    HELD("в руке"),
    EQUIPPED("экипирован"),
    CONSUMED("при использовании"),
    MANUAL("вручную");

    private final String name;
}
