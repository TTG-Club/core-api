package club.ttg.dnd5.dto.base;

import java.util.Map;

public interface NumberFormatted<T> {
    Long BETWEEN_TWO_AND_FOUR = 2L;
    Long GREATER_THAN_FOUR = 5L;
    Map<T, Map<Long, String>> getConjugated();
    String getName();

    default Boolean getMeasurable() {
        return getConjugated().containsKey(this);
    }

    default String getFormattedName(Long number){
        if(getConjugated().containsKey(this)){
            long lastDigit = number % 10;
            long lastTwoDigits = number % 100;
            if (lastDigit == 1 && lastTwoDigits != 11) {
                return getName();
            } else if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
                return getConjugated().get(this).get(BETWEEN_TWO_AND_FOUR);
            } else {
                return getConjugated().get(this).get(GREATER_THAN_FOUR);
            }
        }
        return getName();
    }
}
