package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Верховые и прочие животные из таблицы Книги игрока, которых держат в конюшне. Цена —
 * обычная стоимость животного, зм. Слон в конюшню не помещается и в таблицу не входит.
 *
 * <p>Имена констант лежат в конюшнях и в приказах, поэтому переименовывать их молча нельзя.</p>
 */
@Getter
@AllArgsConstructor
public enum StableAnimalKind {
    CAMEL("Верблюд", "Camel", Size.LARGE, 50),
    DRAFT_HORSE("Тяжеловоз", "Draft Horse", Size.LARGE, 50),
    MASTIFF("Мастиф", "Mastiff", Size.MEDIUM, 25),
    MULE("Мул", "Mule", Size.MEDIUM, 8),
    PONY("Пони", "Pony", Size.MEDIUM, 30),
    RIDING_HORSE("Ездовая лошадь", "Riding Horse", Size.LARGE, 75),
    WARHORSE("Боевой конь", "Warhorse", Size.LARGE, 400);

    private final String name;
    /** Английское название: по нему животное ищется в бестиарии. */
    private final String english;
    private final Size size;
    private final int cost;
}
