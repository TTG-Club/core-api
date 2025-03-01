package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesShortResponse extends ShortResponse {
    private String image;
}
