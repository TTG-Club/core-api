package club.ttg.dnd5.domain.update.rest.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ChangeAction {
    private String color;
    private String name;
    public ChangeAction(ChangeActionType type) {
        this.color = type.getColor();
        this.name = type.getName();
    }
}
