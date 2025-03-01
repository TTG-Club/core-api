package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MagicSchool {
    ABJURATION("ограждения"),
    CONJURATION("вызова"),
    DIVINATION("прорицания"),
    ENCHANTMENT("очарования"),
    EVOCATION("воплощения"),
    ILLUSION("иллюзии"),
    NECROMANCY("некромантии"),
    TRANSMUTATION("преобразования")
    ;

    private final String name;

    public static MagicSchool parse(String name) {
        for (MagicSchool school : values()) {
            if (school.name.equalsIgnoreCase(name)) {
                return school;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильное название школы магии");
    }
}
