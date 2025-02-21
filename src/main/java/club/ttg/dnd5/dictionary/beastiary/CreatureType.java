package club.ttg.dnd5.dictionary.beastiary;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Типы существ из бестиария
 */
public enum CreatureType {
	ABERRATION("Аберрация"),
	BEAST("Зверь"),
	CELESTIAL("Небожитель"),
	CONSTRUCT("Конструкт"),
	DRAGON("Дракон"),
	ELEMENTAL("Элементаль"),
	FEY("Фея", "Йей"),
	FIEND("Исчадие"),
	GIANT("Великан", "Гигант"),
	HUMANOID("Гуманоид"),
	MONSTROSITY("Монстр", "Чудовище"),
	PLANT("Растение"),
	UNDEAD("Нежить"),
	SLIME("Слизь"),
	SWARM("Рой", "Стая");

	private final String displayCyrillicName;
	private final Set<String> cyrillicNames;

	CreatureType(String... cyrillicNames) {
		this.displayCyrillicName = cyrillicNames[0];
		this.cyrillicNames = new HashSet<>(Arrays.asList(cyrillicNames));
	}

	public String getCyrillicName() {
		return this.displayCyrillicName;
	}

	public static CreatureType parse(final String type) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		return Arrays.stream(values())
				.filter(t -> t.name().equalsIgnoreCase(type))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid type: " + type));
	}


	public static Set<CreatureType> getFilterTypes() {
		return EnumSet.of(ABERRATION, BEAST, CELESTIAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, FIEND, GIANT, HUMANOID,
				MONSTROSITY, PLANT, UNDEAD, SLIME);
	}
}
