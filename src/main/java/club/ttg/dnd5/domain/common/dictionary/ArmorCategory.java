package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArmorCategory {
	LIGHT("Легкий доспех", "1 минута", "1 минута"),
	MEDIUM("Средний доспех", "5 минут", "1 минута"),
	HEAVY("Тяжелый доспех", "10 минут", "5 минут"),
	SHIELD("Щит", "1 действие", "1 действие");

	private final String name;
	/**
	 * Время надевания
	 */
	private final String putting;
	/**
	 * Время снятия
	 */
	private final String removal;
}
