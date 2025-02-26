package club.ttg.dnd5.domain.common.dictionary;

import lombok.Getter;

/**
 * Характеристики
 */
@Getter
public enum Ability {
    STRENGTH("Сила", "Силы"),
    DEXTERITY("Ловкость", "Ловкости"),
    CONSTITUTION("Телосложение", "Телосложения"),
    INTELLIGENCE("Интеллект", "Интеллекта"),
    WISDOM("Мудрость", "Мудрости"),
    CHARISMA("Харизма", "Харизмы");

    private final String[] names;

    Ability(final String... names) {
        this.names = names;
    }
    public String getName() {
        return names[0];
    }

    public String getShortName() {
        return switch (this) {
            case STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE ->
                    names[0].substring(0, 3);
            case WISDOM -> "Мдр";
            default -> "";
        };
    }

    public static Ability parseName(final String abilityName) {
        for (Ability ability : values()) {
            for (String name : ability.getNames()) {
                if (name.equalsIgnoreCase(abilityName)) {
                    return ability;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported ability name");
    }

    public static Ability parseShortName(String shortName) {
        return switch (shortName) {
            case "Сил" -> Ability.STRENGTH;
            case "Лов" -> Ability.DEXTERITY;
            case "Тел" -> Ability.CONSTITUTION;
            case "Инт" -> Ability.INTELLIGENCE;
            case "Мдр", "Муд" -> Ability.WISDOM;
            case "Хар" -> Ability.CHARISMA;
            default -> null;
        };
    }

    /**
     * Получение модификатора характеристики
     * @param ability значение характеристики
     * @return модификатор характеристики
     */
    public static byte getModifier(byte ability) {
        return (byte) ((ability - 10) < 0 ? (ability - 11) / 2 : (ability - 10) / 2);
    }
}