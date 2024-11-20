package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedSpeciesDto {
    NameBasedDTO name;
    String url;
}
