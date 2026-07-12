package club.ttg.dnd5.domain.tool.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParticipantType {
    PLAYER("Игрок"),
    CREATURE("Существо");

    private final String name;
}
