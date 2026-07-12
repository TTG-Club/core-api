package club.ttg.dnd5.domain.tool.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrackerStatus {
    PREPARING("Подготовка"),
    ACTIVE("Бой");

    private final String name;
}
