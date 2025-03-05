package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link club.ttg.dnd5.domain.species.model.SpeciesSize}
 */
@Getter
@Setter
public class SpeciesSizeDto implements Serializable {
    private Size type;
    private Short from;
    private Short to;
}