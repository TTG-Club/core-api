package club.ttg.dnd5.domain.book.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@AllArgsConstructor
@Getter
@ToString
public enum TypeBook {
    OFFICIAL("Базовые", "Официальные источники", "Basic"),
    MODULE("Приключения",  "Официальные источники", "Basic"),
    SETTING("Сеттинги", "Официальные источники", "Basic"),
    TEST("Unearthed Arcana", "Тестовый материал", "UA"),
    THIRD_PARTY("3rd party",  "Контент от третьих лиц", "3rd"),
    CUSTOM("Homebrew", "Самоделка", "HB");

    private final String name;
    private final String group;
    private final String label;

    public static TypeBook parse(String type) {
        return Arrays.stream(values())
                .filter(t -> t.name.equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type: " + type));
    }

    public String getName() {
        return name;
    }
}
