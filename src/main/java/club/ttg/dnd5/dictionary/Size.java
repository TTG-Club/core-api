package club.ttg.dnd5.dictionary;

import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum Size {
	TINY("Крошечный","Крошечная", "Крошечное"),
	SMALL("Маленький", "Маленькая", "Маленькое"),
	MEDIUM("Средний", "Средняя", "Среднее"),
	LARGE("Большой", "Большая", "Большое"),
	HUGE("Огромный", "Огромная", "Огромное"),
	GARGANTUAN("Громадный", "Громадная", "Громадное");

	private final String [] names;

	Size(String... names){
		this.names = names;
	}

	public static Size parse(String size) {
		for (Size creatureSize : values()) {
			for (String sizeName : creatureSize.names) {
				if (sizeName.equalsIgnoreCase(size)) {
					return creatureSize;
				}
			}
		}
		return null;
	}

	public static Set<Size> getFilterSizes(){
		return EnumSet.of(TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN);
	}

	public String getSizeName(CreatureType type) {
		return switch (type) {
			case ABERRATION, FEY, UNDEAD, SLIME, FIEND, PLANT -> names[2];
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
