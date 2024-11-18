package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateSpeciesDto extends BaseDTO {
    private CreaturePropertiesDto creatureProperties = new CreaturePropertiesDto();
    private Collection<SpeciesFeatureDto> features = new ArrayList<>();
}
