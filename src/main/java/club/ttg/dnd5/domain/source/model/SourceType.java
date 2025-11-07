package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum SourceType {
    OFFICIAL("Базовые", "Официальные источники", "Basic"),
    SETTING("Сеттинги", "Официальные источники", "Basic"),
    MODULE("Приключения",  "Официальные источники", "Basic"),
    TEST("Unearthed Arcana", "Тестовый материал", "UA"),
    THIRD_PARTY("3rd party",  "Контент от третьих лиц", "3rd"),
    CUSTOM("Homebrew", "Самоделка", "HB");

    private final String name;
    private final String group;
    private final String label;

}
