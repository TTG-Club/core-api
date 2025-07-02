package club.ttg.dnd5.domain.beastiary.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureLairRequest {
    private String description;
    private Collection<ActionRequest> effects;
}
