package club.ttg.dnd5.dictionary;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static club.ttg.dnd5.dictionary.Ability.*;

/**
 * Навыки
 */
public enum Skill {
	ATHLETICS (STRENGTH, "Атлетика"),
	ACROBATICS (DEXTERITY, "Акробатика"),
	SLEIGHT_OF_HAND (DEXTERITY, "Лoвкость рук", "Ловкость Рук"),
	STEALTH (DEXTERITY, "Скрытность"),
	ARCANA (INTELLIGENCE, "Магия"),
	HISTORY (INTELLIGENCE, "История"),
	INVESTIGATION (INTELLIGENCE, "Анализ", "Расследование"),
	NATURE (INTELLIGENCE, "Природа", "Естествознание"),
	RELIGION (INTELLIGENCE, "Религия"),
	ANIMAL_HANDLING (WISDOM, "Уход за животными", "Обращение с животными"),
	INSIGHT (WISDOM,"Проницательность"),
	MEDICINE (WISDOM, "Медицина"),
	PERCEPTION (WISDOM, "Внимательность", "Восприятие"),
	SURVIVAL (WISDOM, "Выживание"), //13
	DECEPTION (CHARISMA, "Обман"), //14
	INTIMIDATION (CHARISMA, "Запугивание", "Устрашение"),
	PERFORMANCE (CHARISMA, "Выступление"),
	PERSUASION (CHARISMA, "Убеждение");

	@Getter()
	private final Ability ability;
	@Getter()
	private final String cyrillicName;

	private final Set<String> cyrillicNames;


	Skill(Ability ability, String ... cyrilicNames){
		this.ability = ability;
		this.cyrillicName = cyrilicNames[0];
		this.cyrillicNames = Arrays.stream(cyrilicNames).collect(Collectors.toSet());
	}

	public static Skill parse(String cyrilicName) {
		for (Skill type : values()) {
			if (type.cyrillicNames.contains(cyrilicName)) {
				return type;
			}
		}
		return null;
	}
	
	public static Map<Ability, List<Skill>> getSkillsToAbbility() {
		return Arrays.stream(values()).collect(sortedGroupingBy(Skill::getAbility));
	}
	
	public static <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(
			Function<T, K> function) {
		return Collectors.groupingBy(function, TreeMap::new, Collectors.toList());
	}
}