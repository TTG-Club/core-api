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
public class SpeciesResponse extends BaseDTO {
    // Включаем свойства существа через DTO
    private CreaturePropertiesDTO creatureProperties;
    // Связанные сущности
    private String parentUrl;
    private Collection<String> subSpeciesUrls;
    private Collection<SpeciesFeatureResponse> features;
    private boolean detail = false;
}

