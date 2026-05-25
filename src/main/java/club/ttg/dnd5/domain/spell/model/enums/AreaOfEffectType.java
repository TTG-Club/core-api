package club.ttg.dnd5.domain.spell.model.enums;

import lombok.Getter;

@Getter
public enum AreaOfEffectType {
    CYLINDER("цилиндр", "высота", "радиус"),
    CONE("конус", "длина"),
    CUBE("куб", "длина стороны"),
    EMANATION("эманация", "расстояние"),
    LINE("линия", "длина","ширина"),
    SPHERE("сфера", "радиус");

    private final String name;
    private final String[] units;

    AreaOfEffectType(String name, String... units) {
        this.name = name;
        this.units = units;
    }
}
