package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static club.ttg.dnd5.domain.common.dictionary.Ability.*;

/**
 * Навыки
 */
@Getter
@AllArgsConstructor
public enum Skill {
	ACROBATICS (DEXTERITY, "Акробатика", "Удержаться на ногах в сложной ситуации или выполнить акробатический трюк."),
	ANIMAL_HANDLING (WISDOM, "Уход за животными", "Успокоить или дрессировать животное, или заставить его вести себя определённым образом."),
	ARCANA (INTELLIGENCE, "Аркана", "Вспомнить знания о заклинаниях, магических предметах и планах бытия."),
	ATHLETICS (STRENGTH, "Атлетика", "Прыгнуть дальше обычного, остаться на плаву в бурной воде или сломать что-то."),
	DECEPTION (CHARISMA, "Обман", "Убедительно солгать или убедительно носить маскировку."), //14
	HISTORY (INTELLIGENCE, "История", "Вспомнить знания об исторических событиях, людях, нациях и культурах."),
	INSIGHT (WISDOM,"Проницательность", "Определить настроение и намерения человека."),
	INTIMIDATION (CHARISMA, "Запугивание", "Внушить трепет или заставить кого-то сделать то, что вам нужно."),
	INVESTIGATION (INTELLIGENCE, "Анализ", "Найти скрытую информацию в книгах или понять, как что-то работает."),
	MEDICINE (WISDOM, "Медицина", "Диагностировать болезнь или определить, что убило недавно погибшего."),
	NATURE (INTELLIGENCE, "Природа", "Вспомнить знания о местности, растениях, животных и погоде."),
	PERCEPTION (WISDOM, "Внимательность", "С использованием комбинации чувств заметить что-то, что легко пропустить."),
	PERFORMANCE (CHARISMA, "Выступление", "Выступить, рассказать историю, исполнить музыку или станцевать."),
	PERSUASION (CHARISMA, "Убеждение", "Честно и вежливо убедить кого-то в чём-то."),
	RELIGION (INTELLIGENCE, "Религия", "Вспомнить знания о богах, религиозных ритуалах и священных символах."),
	SLEIGHT_OF_HAND (DEXTERITY, "Ловкость рук", "Обыскать карман, спрятать предмет в руке или выполнить фокус."),
	STEALTH (DEXTERITY, "Скрытность","Остаться незамеченным, передвигаясь тихо и прячась за объектами."),
	SURVIVAL (WISDOM, "Выживание", "Идти по следам, собирать съедобные растения, найти тропу или избежать природных опасностей.");

	private final Ability ability;

	private final String name;
	private final String example;

	public static <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(
			Function<T, K> function) {
		return Collectors.groupingBy(function, TreeMap::new, Collectors.toList());
	}
}