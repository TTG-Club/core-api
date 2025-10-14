package club.ttg.dnd5.domain.item.model.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Свойства оружия
 */
@AllArgsConstructor
@Getter
public enum Property {
    AMMUNITION("Боеприпасы"),
    FINESSE("Фехтовальное"),
    HEAVY("Тяжёлое"),
    LIGHT("Лёгкое"),
    LOADING("Перезарядка"),
    RANGE("Дистанция"),
    REACH("Досягаемость"),
    THROWN("Метательное"),
    TWO_HANDED("Двуручное"),
    VERSATILE("Универсальное"),
    BURST_FIRE("Очередь");

    private final String name;

}
