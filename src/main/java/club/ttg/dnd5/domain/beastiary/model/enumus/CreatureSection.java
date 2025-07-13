package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CreatureSection {
    UNDERDARK("Подземье"),
    UNDERWATER("Под водой"),
    ARCTIC("Полярная тундра"),
    FOREST("Лес"),
    HILL("Холмы"),
    MOUNTAIN("Горы"),
    SWAMP("Болото"),
    COASTAL(""),
    DESERT("Пустыня"),
    GRASSLAND("Равнина/луг"),
    URBAN("Город");

    private final String name;

    public static CreatureSection parse(String name) {
        for (CreatureSection section : values()) {
            if (section.name.equalsIgnoreCase(name)) {
                return section;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильное место обитания");
    }
}
