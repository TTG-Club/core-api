package club.ttg.dnd5.model.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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
}
