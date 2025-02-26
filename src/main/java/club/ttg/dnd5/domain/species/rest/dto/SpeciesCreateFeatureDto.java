package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dto.NameDto;
import club.ttg.dnd5.dto.base.create.SourceReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeciesCreateFeatureDto {
    private NameDto name = new NameDto();
    private String description;
    private SourceReference source = new SourceReference();
}
