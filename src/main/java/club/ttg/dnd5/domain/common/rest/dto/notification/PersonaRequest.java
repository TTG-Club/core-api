package club.ttg.dnd5.domain.common.rest.dto.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaRequest {
    private String id;
    private String name;
    private String image;
    private boolean disabled;
}
