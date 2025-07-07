package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Характеристики
 */
@AllArgsConstructor
@Getter
public enum Ability {
    STRENGTH("Сила", "Сил.", "str"),
    DEXTERITY("Ловкость", "Лов.", "dex"),
    CONSTITUTION("Телосложение", "Тел.", "con"),
    INTELLIGENCE("Интеллект", "Инт.", "int"),
    WISDOM("Мудрость", "Мдр.", "wis"),
    CHARISMA("Харизма", "Хар.", "chr");

    private final String name;
    private final String shortName;
    private final String key;
}