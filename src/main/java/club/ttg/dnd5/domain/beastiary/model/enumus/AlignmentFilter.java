package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AlignmentFilter {
    LAWFUL_GOOD("Законно добрый"),
    NEUTRAL_GOOD("Нейтрально добрый"),
    CHAOTIC_GOOD("Хаотично добрый"),
    LAWFUL_NEUTRAL("Законно нейтральный"),
    CHAOTIC_NEUTRAL("Хаотично нейтральный"),
    TRUE_NEUTRAL("Законно нейтральный"),
    LAWFUL_EVIL("Законно злой"),
    NEUTRAL_EVIL("Нейтрально злой"),
    CHAOTIC_EVIL("Хаотично злой");

    private final String name;

    public static AlignmentFilter parse(String name) {
        for (AlignmentFilter alignment : values()) {
            if (alignment.name.equalsIgnoreCase(name)) {
                return alignment;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильный размер существа");
    }
}
