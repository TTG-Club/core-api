package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum AreaOfEffectType {
    CYLINDER("цилиндр"),
    CONE("конус"),
    CUBE("куб"),
    EMANATION("эманация"),
    LINE("линия"),
    SPHERE("сфера"),
    SQUARE("квадрат"),
    SQUARE_FEET("квадратных футов")
    ;

    private final String name;
}
