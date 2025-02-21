package club.ttg.dnd5.dictionary.item;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemType {
    AMMUNITION("Боеприпасы"),

    ADVENTURING_GEAR("Снаряжение приключенца"),
    ARTISAN_S_TOOLS("Инструменты ремесленников"),
    FOOD_AND_DRINK("Еда и питье"),
    GAMING_SET("Игровой набор"),

    WEAPON("Оружие"),
    MARTIAL_WEAPON("Воинское оружие"),
    SIMPLE_WEAPON("Простое оружие"),
    MELEE_WEAPON("Рукопашное оружие"),
    RANGED_WEAPON("Дальнобойное оружие"),
    FIREARM("Огнестрельное оружие"),

    ARMOR("Доспехи"),
    LIGHT_ARMOR("Легкий доспех"),
    MEDIUM_ARMOR("Средний доспех"),
    HEAVY_ARMOR("Тяжелый доспех"),
    SHIELD("щит"),  // 10

    TOOL("Инструменты"),
    INSTRUMENT("Музыкальные инструменты"),
    SPELLCASTING_FOCUS(""),
    POISON("Яды"),
    MOUNT("Верховое животное"),
    TACK_AND_HARNESS("Упряжь и сбруя"),
    VEHICLE("Транспорт"),
    VEHICLE_AIR("Транспорт (воздушный)"),
    VEHICLE_LAND("Транспорт (наземный)"),
    VEHICLE_WATER("Транспорт (водный)"),

    WAND("волшебная палочка"), // 2
    ROD("жезл"),  // 3
    STAFF("посох"), //4
    POTION("зелье"), //5
    RING("кольцо"),  //6
    SCROLL("свиток"), // 7
    SUBJECT("чудесный предмет"), // 8
    ;

    private final String name;
}
