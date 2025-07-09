package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CreatureSize {
    TINY("Крошечный"),
    SMALL("Маленький"),
    MEDIUM("Средний"),
    LARGE("Большой"),
    HUGE("Огромный"),
    GARGANTUAN("Громадный");

    private final String name;

    public static CreatureSize parse(String name) {
        for (CreatureSize size : values()) {
            if (size.name.equalsIgnoreCase(name)) {
                return size;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильный размер существа");
    }
}
