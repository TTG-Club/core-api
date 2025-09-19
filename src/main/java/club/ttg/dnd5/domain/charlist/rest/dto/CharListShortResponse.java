package club.ttg.dnd5.domain.charlist.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharListShortResponse {
    private String name;
    private String description;
    private String createdAt;
    private String lastModified;
}
