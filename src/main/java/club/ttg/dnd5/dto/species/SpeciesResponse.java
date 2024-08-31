package club.ttg.dnd5.dto.species;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class SpeciesResponse {
    private String url;
    private String name;
    private String english;
    private String alternative;
    private String description;
    private Short page;

    // Включаем свойства существа через DTO
    private CreaturePropertiesDTO creatureProperties;

    // Связанные сущности
    private SpeciesResponse parent;
    private Collection<SpeciesResponse> subSpecies;
    private Collection<SpeciesFeatureResponse> features;
    private boolean detail;
}

