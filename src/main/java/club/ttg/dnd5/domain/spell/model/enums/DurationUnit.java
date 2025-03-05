package club.ttg.dnd5.domain.spell.model.enums;


import club.ttg.dnd5.dto.base.NumberFormatted;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum DurationUnit implements NumberFormatted<DurationUnit> {
    INSTANT("мгновенно"),
    MINUTE("минута"),
    HOUR("час"),
    DAY("день"),
    YEAR("год"),
    UNTIL_DISPEL("пока не рассеется"),
    PERMANENT("постоянно"),
    ROUND("раунд");

    private static final Map<DurationUnit, Map<Long, String>> CONJUGATED_UNITS = Map.of(
            MINUTE, Map.of(BETWEEN_TWO_AND_FOUR, "минуты", GREATER_THAN_FOUR, "минут"),
            HOUR, Map.of(BETWEEN_TWO_AND_FOUR, "часа", GREATER_THAN_FOUR, "часов"),
            DAY, Map.of(BETWEEN_TWO_AND_FOUR, "дня", GREATER_THAN_FOUR, "дней"),
            YEAR, Map.of(BETWEEN_TWO_AND_FOUR, "года", GREATER_THAN_FOUR, "лет"));

    private final String name;

    @Override
    public Map<DurationUnit, Map<Long, String>> getConjugated() {
        return CONJUGATED_UNITS;
    }

}
