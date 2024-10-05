package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.model.base.HasTags;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SpeciesFeatureResponse extends BaseDTO implements HasTags {
    private String description;
    private Map<String, String> tags;
}