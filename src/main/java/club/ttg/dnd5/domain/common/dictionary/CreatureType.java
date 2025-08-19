package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
	SWARM_OF_TINY_UNDEAD("Рой крошечной нежити"),
	SWARM_OF_TINY_MONSTROSITIES("рой крошечных монстров"),
	SWARM_OF_TINY_BEASTS("Рой крошечных зверей"),
	SWARM_OF_SMALL_FIENDS("Рой маленьких исчадий"),
	SWARM_OF_MEDIUM_FIENDS("Рой средних исчадий"),
	;

	private final String name;
}
