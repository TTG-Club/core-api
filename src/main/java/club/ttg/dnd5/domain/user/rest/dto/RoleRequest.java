package club.ttg.dnd5.domain.user.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequest {
    private String name;
    private String description;
}
