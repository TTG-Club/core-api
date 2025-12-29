package club.ttg.dnd5.domain.update.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChangeActionType {
    ADDED("Добавлено"),
    UPDATED("Обновлено"),
    DELETED("Удалено");

    private final String name;
}
