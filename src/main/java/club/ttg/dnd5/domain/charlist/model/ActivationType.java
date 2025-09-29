package club.ttg.dnd5.domain.charlist.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivationType {
    ACTION("действие"),
    NO_ACTION("не требуется"),
    BONUS("бонусное действие"),
    REACTION("реакция"),
    MINUTE("минута"),
    HOUR("час"),
    SPECIAL("особое");
    private final String name;
}
