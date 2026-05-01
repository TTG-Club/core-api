package club.ttg.dnd5.domain.source.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SourceKind {
    SOURCEBOOK("Базовые"),
    SETTING("Сеттинги"),
    ADVENTURE("Приключения");

    private final String name;
}
