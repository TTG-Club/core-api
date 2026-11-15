package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Категория источника: что это за книга, независимо от того, кто её выпустил.
 */
@AllArgsConstructor
@Getter
@ToString
public enum SourceCategory {
    CORE("Основные правила"),
    SETTING("Сеттинг"),
    ADVENTURE("Приключения");

    private final String name;
}
