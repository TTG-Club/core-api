package club.ttg.dnd5.domain.magic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MagicItemCategory {
    WEAPON("Оружие"),
    ARMOR("Доспех"),
    WAND("Волшебная палочка"),
    ROD("Жезл"),
    STAFF("Посох"),
    POTION("Зелье"),
    RING("Кольцо"),
    SCROLL("Свиток"),
    SUBJECT("Чудесный предмет");

    private final String name;
}
