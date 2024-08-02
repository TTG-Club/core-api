package club.ttg.dnd5.dictionary;

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
	public String getShortName() {
		switch (this) {
			case STRENGTH:
			case DEXTERITY:
			case CONSTITUTION:
			case INTELLIGENCE:
				return names[0].substring(0,3);
			case WISDOM:
				return "Мдр";
			default:
				return "";
		}
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
	

	public static byte getModifier(byte ability) {
		return (byte) ((ability - 10) < 0 ? (ability - 11) / 2 : (ability - 10) / 2);
	}
}