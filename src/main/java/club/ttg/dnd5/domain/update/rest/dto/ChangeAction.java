package club.ttg.dnd5.domain.update.rest.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ChangeAction {
    private ChangeActionType type;
    private String name;
    public ChangeAction(ChangeActionType type) {
        this.type = type;
        this.name = type.getName();
    }
}
