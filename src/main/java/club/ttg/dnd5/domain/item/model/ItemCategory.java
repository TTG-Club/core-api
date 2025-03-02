package club.ttg.dnd5.domain.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemCategory {
    ITEM("Снаряжение"),
    ARMOR("Доспех"),
    WEAPON("Оружие"),
    MAGIC("Магический предмет"),
    VEHICLE("Транспортное средство"),
    MOUNT("Верховое животное"),
    TOOL("Инструмент");

    private final String name;
}
