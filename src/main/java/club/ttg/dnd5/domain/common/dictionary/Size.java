package club.ttg.dnd5.domain.common.dictionary;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum Size {
	UNDEFINED("Неопределенный", "Неопределенная", "Неопределенное"),
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
		if (size == null) {
			return UNDEFINED;
		}

		return Arrays.stream(values())
				.filter(s -> s.name().equalsIgnoreCase(size))
				.findFirst()
				.orElse(UNDEFINED);
	}

	public static String convertSizeToEntityFormat(Collection<String> sizes) {
		List<Size> list = sizes.stream()
				.map(Size::parse)
				.toList();
		return list.stream()
				.map(Size::name)
				.collect(Collectors.joining(", "));
	}

	public static List<String> convertEntityFormatToDtoFormat(String entityFormat) {
		return Arrays.stream(entityFormat.split(","))
				.map(String::trim)
				.map(Size::parse)
				.map(s -> s.getNames()[0])
				.toList();
	}

	public static Set<Size> getFilterSizes(){
		return EnumSet.of(TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN);
	}

	public String getSizeName(BeastType type) {
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
