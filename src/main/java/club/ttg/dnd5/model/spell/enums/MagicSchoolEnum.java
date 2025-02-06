package club.ttg.dnd5.model.spell.enums;

import club.ttg.dnd5.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MagicSchoolEnum {
    CONJURATION("вызов", 0),
    EVOCATION("воплощение",  1),
    ILLUSION("иллюзия", 2),
    NECROMANCY("некромантия", 3),
    ABJURATION("ограждение", 4),
    ENCHANTMENT("очарование", 5),
    TRANSMUTATION("преобразование", 6),
    DIVINATION("прорицание", 7);

    private String name;
    private int code;

    MagicSchoolEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static MagicSchoolEnum getMagicSchool(String name) {
        for (MagicSchoolEnum school : values()) {
            if (school.name.equalsIgnoreCase(name)) {
                return school;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильное название школы магии");
    }
}
