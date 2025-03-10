package club.ttg.dnd5.dto.base;

import java.util.Map;
import java.util.Objects;

public interface NumberFormatted<T> {
    Long BETWEEN_TWO_AND_FOUR = 2L;
    Long GREATER_THAN_FOUR = 5L;
    String NUMBER_FORMATTED_TEMPLATE = "%s %s";

    Map<T, Map<Long, String>> getConjugated();

    String getName();

    default Boolean getMeasurable() {
        return getConjugated().containsKey(this);
    }

    default String getFormattedName(Long number) {
        if (Objects.isNull(number)) {
            return getName();
        }
        if (getConjugated().containsKey(this)) {
            long lastDigit = number % 10;
            long lastTwoDigits = number % 100;
            if (lastDigit == 1 && lastTwoDigits != 11) {
                return String.format(NUMBER_FORMATTED_TEMPLATE, number, getName());
            } else if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
                return String.format(NUMBER_FORMATTED_TEMPLATE, number, getConjugated().get(this).get(BETWEEN_TWO_AND_FOUR));
            } else {
                return String.format(NUMBER_FORMATTED_TEMPLATE, number, getConjugated().get(this).get(GREATER_THAN_FOUR));
            }
        }
        return getName();
    }

    default String getGenetiveFormattedName(Long number) {
        if (Objects.isNull(number)) {
            return getName();
        }
        if (getConjugated().containsKey(this)) {
            long lastDigit = number % 10;
            if (lastDigit == 1) {
                return String.format(NUMBER_FORMATTED_TEMPLATE, number, getConjugated().get(this).get(BETWEEN_TWO_AND_FOUR));
            }
             return String.format(NUMBER_FORMATTED_TEMPLATE, number, getConjugated().get(this).get(GREATER_THAN_FOUR));
        }
        return getName();
    }
}
