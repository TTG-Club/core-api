package club.ttg.dnd5.domain.common.dictionary;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

import static club.ttg.dnd5.domain.common.dictionary.Dice.*;

@Getter
public enum Size {
	UNDEFINED(null, 0f, "Неопределенный", "Неопределенная", "Неопределенное"),
	TINY(d4, 2.5f,"Крошечный","Крошечная", "Крошечное"),
	SMALL(d6,3.5f, "Маленький", "Маленькая", "Маленькое"),
	MEDIUM(d8, 4.5f, "Средний", "Средняя", "Среднее"),
	LARGE(d10, 5.5f, "Большой", "Большая", "Большое"),
	HUGE(d12, 6.5f, "Огромный", "Огромная", "Огромное"),
	GARGANTUAN(d20, 10.5f, "Громадный", "Громадная", "Громадное");

	private final String [] names;
	private final Dice hitDice;
	private final float hitAverage;

	Size(Dice hitDace, float hitAverage, String... names){
		this.hitDice = hitDace;
		this.hitAverage = hitAverage;
		this.names = names;
	}

	public static Size parse(String size) {
		if (size == null) {
			return UNDEFINED;
		}
		return Arrays.stream(values())
				.filter(s -> s.name().equalsIgnoreCase(size))
				.findFirst()
				.orElse(UNDEFINED);
	}

	public String getSizeName(CreatureType type) {
		return switch (type) {
			case ABERRATION, FEY, UNDEAD, SLIME -> names[1];
			case FIEND, PLANT -> names[2];
			default -> names[0];
		};
	}

	public String getName() {
		return names[0];
	}

	public String getCell() {
		return switch (this) {
			case TINY -> "1/4 клетки";
			case SMALL -> "1 клетка";
			case MEDIUM -> "1 клетка";
			case LARGE -> "2x2 клетки";
			case HUGE -> "3x3 клетки";
			case GARGANTUAN -> "4х4 клетки или больше";
			default -> "-";
		};
	}
}
