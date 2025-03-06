package club.ttg.dnd5.domain.spell.model.enums;

import club.ttg.dnd5.dto.base.NumberFormatted;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum CastingUnit implements NumberFormatted<CastingUnit> {
    BONUS("бонусное действие"),
    REACTION("реакция"),
    ACTION("действие"),
    ROUND("ход"),
    MINUTE("минута"),
    RITUAL("ритуал"),
    HOUR("час");

    private final String name;
    private static final Map<CastingUnit, Map<Long, String>> CONJUGATED_UNITS = Map.of(
            MINUTE, Map.of(BETWEEN_TWO_AND_FOUR, "минуты", GREATER_THAN_FOUR, "минут"),
            HOUR, Map.of(BETWEEN_TWO_AND_FOUR, "часа", GREATER_THAN_FOUR, "часов"));

    @Override
    public Map<CastingUnit, Map<Long, String>> getConjugated() {
        return CONJUGATED_UNITS;
    }
}
