package club.ttg.dnd5.util;

import com.ibm.icu.text.PluralRules;
import java.util.Locale;

public class PluralUtil {

    private static final PluralRules ruRules =
            PluralRules.forLocale(Locale.of("ru", "RU"));

    /**
     * Получение правильного окончания слова в зависимости от числа (пример: 1 день, 2 дня, 5 дней).
     *
     * @param number Число перед словом с окончанием.
     * @param forms Варианты окончаний слов (для чисел 1, 2, 5).
     * @return Слово из списка с правильным окончанием.
     */
    public static String getPlural(double number, String[] forms) {
        String rule = ruRules.select(Math.abs(number));

        return switch (rule) {
            case "one" -> forms[0];   // 1 день
            case "few" -> forms[1];   // 2 дня
            case "many" -> forms[2];  // 5 дней
            case "other" -> forms[1]; // 1.5 дня (дробные)
            default -> forms[2];
        };
    }
}
