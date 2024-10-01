package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SpeciesResponse extends BaseDTO implements DetailableDTO {
    // Включаем свойства существа через DTO
    private CreaturePropertiesDTO creatureProperties = new CreaturePropertiesDTO();
    // Связанные сущности
    private String parentUrl;
    private Collection<String> subSpeciesUrls;
    private Collection<SpeciesFeatureResponse> features;
    private boolean detail = false;

    @Override
    public void hideDetails() {
        if (!detail) {
            this.creatureProperties = null;
            this.parentUrl = null;
            this.subSpeciesUrls = null;
            this.features = null;
        }
    }

}

