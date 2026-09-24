package club.ttg.dnd5.domain.bastion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Вид сооружения бастиона. */
@Getter
@AllArgsConstructor
public enum FacilityCategory {
    /** Покупается за деньги и время, игровых эффектов не даёт. */
    BASIC("базовое сооружение"),
    /** Получается с уровнем персонажа, выполняет приказы бастиона. */
    SPECIAL("специализированное сооружение");

    private final String name;
}
