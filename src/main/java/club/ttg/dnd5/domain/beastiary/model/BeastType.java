package club.ttg.dnd5.domain.beastiary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Типы существ из бестиария
 */
@Getter
@AllArgsConstructor
public enum BeastType {
	ABERRATION("Аберрация"),
	BEAST("Зверь"),
	CELESTIAL("Небожитель"),
	CONSTRUCT("Конструкт"),
	DRAGON("Дракон"),
	ELEMENTAL("Элементаль"),
	FEY("Фея"),
	FIEND("Исчадие"),
	GIANT("Великан"),
	HUMANOID("Гуманоид"),
	MONSTROSITY("Монстр"),
	PLANT("Растение"),
	UNDEAD("Нежить"),
	SLIME("Слизь"),
	SWARM("Рой");

	private final String name;


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
