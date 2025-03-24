package club.ttg.dnd5.dto.base;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

public interface NumberFormatted<T> {
    Long BETWEEN_TWO_AND_FOUR = 2L;
    Long GREATER_THAN_FOUR = 5L;
    String NUMBER_FORMATTED_TEMPLATE = "%d %s";

    Map<T, Map<Long, String>> getConjugated();

    String getName();

    /**
     * Возвращает true если измеримый
     * @return true если измеримый, иначе false
     */
    default boolean getMeasurable() {
        return getConjugated().containsKey(this);
    }

    /**
     * Возвращает отформатированное значение (число наименование или просто капитализированное наименование)
     * @param number количество, если есть
     * @return отформатированное значение (число наименование или просто капитализированное наименование)
     */
    default String getFormattedName(Long number) {
        if (Objects.isNull(number) || !getMeasurable()) {
            return StringUtils.capitalize(getName());
        }
        Map<Long, String> conjugatedMap = getConjugated().getOrDefault(this, Map.of());
        long lastDigit = number % 10;
        long lastTwoDigits = number % 100;

        String nameVariant;
        if (lastDigit == 1 && lastTwoDigits != 11) {
            nameVariant = getName();
        } else if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
            nameVariant = conjugatedMap.getOrDefault(BETWEEN_TWO_AND_FOUR, getName());
        } else {
            nameVariant = conjugatedMap.getOrDefault(GREATER_THAN_FOUR, getName());
        }

        return String.format(NUMBER_FORMATTED_TEMPLATE, number, nameVariant);
    }

    default String getGenitiveFormattedName(Long number) {
        if (Objects.isNull(number)) {
            return getName();
        }

        Map<Long, String> conjugatedMap = getConjugated().getOrDefault(this, Map.of());
        long lastDigit = number % 10;
        String nameVariant = (lastDigit == 1)
                ? conjugatedMap.getOrDefault(BETWEEN_TWO_AND_FOUR, getName())
                : conjugatedMap.getOrDefault(GREATER_THAN_FOUR, getName());

        return String.format(NUMBER_FORMATTED_TEMPLATE, number, nameVariant);
    }
}
