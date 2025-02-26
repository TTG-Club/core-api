package club.ttg.dnd5.domain.spell.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimeUnit {
    BONUS("бонусное действие"),
    REACTION("реакция"),
    ACTION("действие"),
    ROUND("ход"),
    MINUTE("минута"),
    HOUR("час");

    private final String name;

    public String getName(int number) {
        return switch (this) {
            case MINUTE -> formatTime(number, "минута", "минуты", "минут");
            case HOUR -> formatTime(number, "час", "часа", "часов");
            default -> name;
        };
    }

    private String formatTime(int number, String singular, String dual, String plural) {
        int lastDigit = number % 10;
        int lastTwoDigits = number % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return plural;
        }

        if (lastDigit == 1) {
            return singular;
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return dual;
        } else {
            return plural;
        }
    }
}