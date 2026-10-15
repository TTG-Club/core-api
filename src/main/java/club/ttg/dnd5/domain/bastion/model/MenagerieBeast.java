package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Существа из таблицы «Существа зверинца»: их наёмники зверинца вербуют без согласия
 * Мастера. Цена — за одно существо, зм.
 *
 * <p>Имена констант лежат в приказах и в существах зверинца, поэтому переименовывать их
 * молча нельзя.</p>
 */
@Getter
@AllArgsConstructor
public enum MenagerieBeast {
    APE("Обезьяна", Size.MEDIUM, 500),
    BLACK_BEAR("Чёрный медведь", Size.MEDIUM, 500),
    BROWN_BEAR("Бурый медведь", Size.LARGE, 1000),
    CONSTRICTOR_SNAKE("Удав", Size.LARGE, 250),
    CROCODILE("Крокодил", Size.LARGE, 500),
    DIRE_WOLF("Лютый волк", Size.LARGE, 1000),
    GIANT_VULTURE("Гигантский гриф", Size.LARGE, 1000),
    HYENA("Гиена", Size.MEDIUM, 50),
    JACKAL("Шакал", Size.SMALL, 50),
    LION("Лев", Size.LARGE, 1000),
    OWLBEAR("Совомед", Size.LARGE, 3500),
    PANTHER("Пантера", Size.MEDIUM, 250),
    TIGER("Тигр", Size.LARGE, 1000);

    private final String name;
    private final Size size;
    private final int cost;
}
