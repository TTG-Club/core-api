package club.ttg.dnd5.domain.book.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@AllArgsConstructor
@Getter
@ToString
public enum TypeBook {
    OFFICIAL("Базовые"),
    MODULE("Приключения"),
    SETTING("Сеттинги"),
    TEST("Unearthed Arcana"),
    THIRD_PARTY("3rd party"),
    CUSTOM("Homebrew");

    private final String name;

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
