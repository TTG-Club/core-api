package club.ttg.dnd5.domain.item.model.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Приёмы оружия
 */
@AllArgsConstructor
@Getter
public enum Mastery {
    CLEAVE("Прорубание"),
    GRAZE("Задевание"),
    NICK("Выпад"),
    PUSH("Толкание"),
    SAP("Изнурение"),
    SLOW("Замедление"),
    TOPPLE("Опрокидывание"),
    VEX("Подавление");

    private final String name;
}
