package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateSpeciesDTO extends BaseDTO {
    private CreaturePropertiesDTO creatureProperties;
    private Collection<SpeciesFeatureResponse> features;
    boolean parent;
}
