package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Типы существ из бестиария
 */
@Getter
@AllArgsConstructor
public enum CreatureType {
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
	SLIME("Слизь"),
	PLANT("Растение"),
	UNDEAD("Нежить"),
	SWARM_OF_TINY_UNDEAD("рой крошечной нежити"),
	SWARM_OF_TINY_MONSTROSITIES("рой крошечных монстров"),
	SWARM_OF_TINY_BEASTS("рой крошечных зверей"),
	SWARM_OF_SMALL_FIENDS("рой маленьких исчадий"),
	SWARM_OF_MEDIUM_FIENDS("рой средних исчадий"),
	;

	private final String name;


	public static CreatureType parse(final String type) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		return Arrays.stream(values())
				.filter(t -> t.name().equalsIgnoreCase(type))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid type: " + type));
	}
}
