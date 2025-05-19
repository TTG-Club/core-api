package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Характеристики
 */
@AllArgsConstructor
@Getter
public enum Ability {
    STRENGTH("Сила", "Сил."),
    DEXTERITY("Ловкость", "Лов."),
    CONSTITUTION("Телосложение", "Тел."),
    INTELLIGENCE("Интеллект", "Инт."),
    WISDOM("Мудрость", "Мдр."),
    CHARISMA("Харизма", "Хар.");

    private final String name;
    private final String shortName;

    /**
     * Получение модификатора характеристики
     * @param ability значение характеристики
     * @return модификатор характеристики
     */
    public static byte getModifier(byte ability) {
        return (byte) ((ability - 10) < 0 ? (ability - 11) / 2 : (ability - 10) / 2);
    }
}