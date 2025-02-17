package club.ttg.dnd5.dictionary.beastiary;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Типы существ из бестиария
 */
public enum BeastType {
	ABERRATION("Аберрация"),
	BEAST("Зверь"),
	CELESTIAL("Небожитель"),
	CONSTRUCT("Конструкт"),
	DRAGON("Дракон"),
	ELEMENTAL("Элементаль"),
	FEY("Фея", "Фей"),
	FIEND("Исчадие"),
	GIANT("Великан", "Гигант"),
	HUMANOID("Гуманоид"),
	MONSTROSITY("Монстр", "Чудовище"),
	PLANT("Растение"),
	UNDEAD("Нежить"),
	SLIME("Слизь"),
	SWARM("Рой", "Стая");

	private final String name;
	private final Set<String> names;

	BeastType(String... cyrillicNames) {
		this.name = cyrillicNames[0];
		this.names = new HashSet<>(Arrays.asList(cyrillicNames));
	}

	public String getCyrillicName() {
		return this.name;
	}

	public static BeastType parse(final String type) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		return Arrays.stream(values())
				.filter(t -> t.name().equalsIgnoreCase(type))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid type: " + type));
	}


	public static Set<BeastType> getFilterTypes() {
		return EnumSet.of(ABERRATION, BEAST, CELESTIAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, FIEND, GIANT, HUMANOID,
				MONSTROSITY, PLANT, UNDEAD, SLIME);
	}
}
