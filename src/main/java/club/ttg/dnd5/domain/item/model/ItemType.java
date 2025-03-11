package club.ttg.dnd5.domain.item.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemType {
    AMMUNITION("Боеприпасы", ItemCategory.ITEM),

    ADVENTURING_GEAR("Снаряжение приключенца", ItemCategory.ITEM),
    ARTISAN_S_TOOLS("Инструменты ремесленников", ItemCategory.ITEM),
    TOOL("Инструменты", ItemCategory.TOOL),
    INSTRUMENT("Музыкальные инструменты", ItemCategory.TOOL),
    FOOD_AND_DRINK("Еда и питье", ItemCategory.ITEM),
    GAMING_SET("Игровой набор", ItemCategory.ITEM),

    WEAPON("Оружие", ItemCategory.WEAPON),
    MARTIAL_WEAPON("Воинское оружие", ItemCategory.WEAPON),
    SIMPLE_WEAPON("Простое оружие", ItemCategory.WEAPON),
    MELEE_WEAPON("Рукопашное оружие", ItemCategory.WEAPON),
    RANGED_WEAPON("Дальнобойное оружие", ItemCategory.WEAPON),
    FIREARM("Огнестрельное оружие", ItemCategory.WEAPON),

    ARMOR("Доспехи", ItemCategory.ARMOR),
    LIGHT_ARMOR("Легкий доспех", ItemCategory.ARMOR),
    MEDIUM_ARMOR("Средний доспех", ItemCategory.ARMOR),
    HEAVY_ARMOR("Тяжелый доспех", ItemCategory.ARMOR),
    SHIELD("Щит", ItemCategory.ARMOR),  // 10

    SPELLCASTING_FOCUS("Магическая фокусировка", ItemCategory.ITEM),
    POISON("Яды", ItemCategory.ITEM),
    MOUNT("Верховое животное", ItemCategory.MOUNT),
    TACK_AND_HARNESS("Упряжь и сбруя", ItemCategory.ITEM),
    VEHICLE("Транспорт", ItemCategory.VEHICLE),
    VEHICLE_AIR("Транспорт (воздушный)", ItemCategory.VEHICLE),
    VEHICLE_LAND("Транспорт (наземный)", ItemCategory.VEHICLE),
    VEHICLE_WATER("Транспорт (водный)", ItemCategory.VEHICLE);

    private final String name;
    private final ItemCategory category;
}
