package club.ttg.dnd5.domain.spell.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AreaOfEffectType {
    CYLINDER("цилиндр"),
    CONE("конус"),
    CUBE("куб"),
    EMANATION("эманация"),
    LINE("линия"),
    SPHERE("сфера");

    private final String name;
}
