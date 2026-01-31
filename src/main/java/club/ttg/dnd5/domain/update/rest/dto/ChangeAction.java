package club.ttg.dnd5.domain.update.rest.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ChangeAction {
    private String type;
    private String color;
    private String name;

    public ChangeAction(ChangeActionType type) {
        this.type = type.name();
        this.color = type.getColor();
        this.name = type.getName();
    }
}
