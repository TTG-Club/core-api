package club.ttg.dnd5.model.spell.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Дистанция заклинания
 */
@Getter
@AllArgsConstructor
public enum DistanceUnit {
    SELF("на себя"),
    TOUCH("касание"),
    FEET("футов"),
    SIGHT("в пределах видимости"),
    MILE("миль"),
    UNLIMITED("неограниченная");

    private final String name;
}
