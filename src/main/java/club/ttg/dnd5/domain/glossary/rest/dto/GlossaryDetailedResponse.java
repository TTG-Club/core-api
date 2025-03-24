package club.ttg.dnd5.domain.glossary.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlossaryDetailedResponse extends GlossaryShortResponse{
    private String description;

    private String alternative;
}
