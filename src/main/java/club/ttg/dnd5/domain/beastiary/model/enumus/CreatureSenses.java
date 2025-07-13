package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CreatureSenses {
    TRUESIGHT("Истинное зрение"),
    BLINDSIGHT("Слепое зрение"),
    DARKVISION("Темное зрение"),
    TREMORSENSE("Чувство вибраций"),
    UNIMPEDED("Темное зрение (даже через магическую тьму)");

    private final String name;

    public static CreatureSenses parse(String name) {
        for (CreatureSenses senses : values()) {
            if (senses.name.equalsIgnoreCase(name)) {
                return senses;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильное чувство");
    }
}
