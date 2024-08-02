package club.ttg.dnd5.dictionary.item;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArmorCategory {
	LIGHT("Легкий доспех", "1 минута", "1 минута"),
	MEDIUM("Средний доспех", "5 минут", "1 минута"),
	HEAVY("Тяжелый доспех", "10 минут", "5 минут"),
	SHIELD("Щит", "1 действие", "1 действие");
	private String name;
	/**
	 * Время надевания
	 */
	private String putting;
	/**
	 * Время снятия
	 */
	private String removal;
}