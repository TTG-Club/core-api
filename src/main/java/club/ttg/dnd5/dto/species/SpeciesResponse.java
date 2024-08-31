package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class SpeciesResponse extends BaseDTO {
    // Включаем свойства существа через DTO
    private CreaturePropertiesDTO creatureProperties;

    // Связанные сущности
    private SpeciesResponse parent;
    private Collection<SpeciesResponse> subSpecies;
    private Collection<SpeciesFeatureResponse> features;
    private boolean detail;
}

