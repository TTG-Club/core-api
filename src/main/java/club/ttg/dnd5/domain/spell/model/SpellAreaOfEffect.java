package club.ttg.dnd5.domain.spell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpellAreaOfEffect {
    CYLINDER("цилиндр"),
    CONE("конус"),
    CUBE("куб"),
    EMANATION("эманация"),
    LINE("линия"),
    SPHERE("сфера");
    private final String name;
}
