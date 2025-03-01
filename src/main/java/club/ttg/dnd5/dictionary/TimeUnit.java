package club.ttg.dnd5.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimeUnit {
	BONUS("бонусное действие", false),
	REACTION("реакция", false),
	ACTION("действие",  false),
	ROUND("ход", false),
	MINUTE("минута", true),
	HOUR("час", true);

	private final String name;
	private boolean hasValue;

	public String getName(int number) {
		switch (this) {
			case MINUTE -> {
				if (number == 10) {
					return "минут";
				} else {
					return name;
				}
			}
			case HOUR -> {
				if (number == 8 || number == 12) {
					return "часов";
				} else if (number == 24) {
					return "часа";
				} else
					return name;
			}
			default -> {
				return name;
			}
		}
	}
}