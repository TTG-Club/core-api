package club.ttg.dnd5.domain.spell.model.enums;

import club.ttg.dnd5.dto.base.NumberFormatted;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Дистанция заклинания
 */
@Getter
@AllArgsConstructor
public enum DistanceUnit implements NumberFormatted<DistanceUnit> {
    SELF("на себя"),
    TOUCH("касание"),
    FEET("фут"),
    SIGHT("в пределах видимости"),
    MILE("миля"),
    UNLIMITED("неограниченная");

    private final String name;

    private static final Map<DistanceUnit, Map<Long, String>> CONJUGATED_UNITS = Map.of(
            FEET, Map.of(BETWEEN_TWO_AND_FOUR, "фута", GREATER_THAN_FOUR, "футов"),
            MILE, Map.of(BETWEEN_TWO_AND_FOUR, "мили", GREATER_THAN_FOUR, "миль")
    );

    @Override
    public Map<DistanceUnit, Map<Long, String>> getConjugated() {
        return CONJUGATED_UNITS;
    }
}
