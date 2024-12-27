package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.SourceDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesCreateFeatureDto {
    private NameBasedDTO name = new NameBasedDTO();
    private String description;
    private SourceDto source = new SourceDto();
}
