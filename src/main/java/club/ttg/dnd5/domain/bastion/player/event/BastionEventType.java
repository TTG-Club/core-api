package club.ttg.dnd5.domain.bastion.player.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Таблица «События бастиона»: Мастер бросает 1к100 сразу после приказа «Обслуживать».
 *
 * <p>Имена констант лежат в журнале событий, переименовывать их молча нельзя.</p>
 */
@Getter
@AllArgsConstructor
public enum BastionEventType {
    ALL_IS_WELL(1, 50, "Всё хорошо"),
    ATTACK(51, 55, "Атака"),
    CRIMINAL_HIRELING(56, 58, "Наёмник оказался преступником"),
    EXTRAORDINARY_OPPORTUNITY(59, 63, "Удачный случай"),
    FRIENDLY_VISITORS(64, 72, "Дружелюбные посетители"),
    GUEST(73, 76, "Гость"),
    LOST_HIRELINGS(77, 79, "Пропавшие наёмники"),
    MAGICAL_DISCOVERY(80, 83, "Волшебное открытие"),
    REFUGEES(84, 91, "Беженцы"),
    REQUEST_FOR_AID(92, 98, "Просьба о помощи"),
    TREASURE(99, 100, "Сокровище");

    private final int from;
    private final int to;
    private final String name;

    /** Событие по броску 1к100. */
    public static BastionEventType of(int roll) {
        return Arrays.stream(values())
                .filter(type -> roll >= type.from && roll <= type.to)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Бросок 1к100 вне таблицы: " + roll));
    }
}
