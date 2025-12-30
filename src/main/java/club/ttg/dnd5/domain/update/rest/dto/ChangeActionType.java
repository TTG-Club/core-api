package club.ttg.dnd5.domain.update.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChangeActionType {
    ADDED("Добавлено", "success"),
    UPDATED("Обновлено", "info"),
    DELETED("Удалено", "error");

    private final String name;
    private final String color;
}
