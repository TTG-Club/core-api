package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CreatureTypes {
    CELESTIAL("Небожитель"),
    HUMANOID("Гуманоид"),
    UNDEAD("Нежить"),
    ABERRATION("Аберации"),
    GIANT("Гиганты"),
    BEAST("Чудовища"),
    DRAGON("Драконы"),
    FIEND("Дьяволы"),
    FEY("Феи"),
    CONSTRUCT("Конструкты"),
    ELEMENTAL("Элементали"),
    SWARM_OF_TINY_BEASTS("Стая мелких существ");

    private final String name;

    public static CreatureTypes parse(String name) {
        for (CreatureTypes types : values()) {
            if (types.name.equalsIgnoreCase(name)) {
                return types;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильный тип существа");
    }
}
