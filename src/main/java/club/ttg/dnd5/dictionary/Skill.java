package club.ttg.dnd5.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static club.ttg.dnd5.dictionary.Ability.*;

/**
 * Навыки
 */
@AllArgsConstructor
@Getter
public enum Skill {
	ATHLETICS (STRENGTH, "Атлетика"),
	ACROBATICS (DEXTERITY, "Акробатика"),
	SLEIGHT_OF_HAND (DEXTERITY, "Лoвкость рук"),
	STEALTH (DEXTERITY, "Скрытность"),
	ARCANA (INTELLIGENCE, "Магия"),
	HISTORY (INTELLIGENCE, "История"),
	INVESTIGATION (INTELLIGENCE, "Анализ"),
	NATURE (INTELLIGENCE, "Природа"),
	RELIGION (INTELLIGENCE, "Религия"),
	ANIMAL_HANDLING (WISDOM, "Уход за животными"),
	INSIGHT (WISDOM,"Проницательность"),
	MEDICINE (WISDOM, "Медицина"),
	PERCEPTION (WISDOM, "Внимательность"),
	SURVIVAL (WISDOM, "Выживание"), //13
	DECEPTION (CHARISMA, "Обман"), //14
	INTIMIDATION (CHARISMA, "Запугивание"),
	PERFORMANCE (CHARISMA, "Выступление"),
	PERSUASION (CHARISMA, "Убеждение");

	private final Ability ability;

	private final String name;


	public static <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(
			Function<T, K> function) {
		return Collectors.groupingBy(function, TreeMap::new, Collectors.toList());
	}
}