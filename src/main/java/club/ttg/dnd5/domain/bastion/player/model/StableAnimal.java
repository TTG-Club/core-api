package club.ttg.dnd5.domain.bastion.player.model;

import club.ttg.dnd5.domain.bastion.model.StableAnimalKind;
import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Животное в конюшне. Лежит в JSONB сооружения и, пока его покупают или продают, — в приказе.
 */
@Getter
@Setter
@NoArgsConstructor
public class StableAnimal {
    /** Ключ животного в конюшне: по нему его продают и убирают. */
    private String id;

    /** Животное из таблицы; {@code null} — другое животное, с согласия Мастера. */
    private StableAnimalKind kind;

    private String name;

    private Size size;

    /** Обычная цена животного, зм: от неё считается цена продажи. */
    private int cost;

    /** Ход бастиона, с которого животное живёт в конюшне. */
    private int arrivedOnTurn;

    /** Животное из таблицы, поселившееся в конюшне на этом ходу. */
    public static StableAnimal of(StableAnimalKind kind, int turn) {
        StableAnimal animal = new StableAnimal();
        animal.setId(UUID.randomUUID().toString());
        animal.setKind(kind);
        animal.setName(kind.getName());
        animal.setSize(kind.getSize());
        animal.setCost(kind.getCost());
        animal.setArrivedOnTurn(turn);
        return animal;
    }

    /**
     * Животные новой конюшни: ездовая лошадь и два пони. Лошадь на верблюда или пони на
     * мулов меняют продажей и покупкой.
     */
    public static List<StableAnimal> starting(int turn) {
        return new ArrayList<>(List.of(of(StableAnimalKind.RIDING_HORSE, turn),
                of(StableAnimalKind.PONY, turn),
                of(StableAnimalKind.PONY, turn)));
    }
}
