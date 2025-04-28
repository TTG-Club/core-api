package club.ttg.dnd5.domain.magic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MagicItemCategory {
    WEAPON("оружие"),
    ARMOR("доспех"),
    WAND("волшебная палочка"),
    ROD("жезл"),
    STAFF("посох"),
    POTION("зелье"),
    RING("кольцо"),
    SCROLL("свиток"),
    SUBJECT("чудесный предмет");

    private final String name;
}
