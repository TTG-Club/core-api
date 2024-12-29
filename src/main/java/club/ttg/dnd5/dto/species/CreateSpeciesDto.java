package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasTagDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateSpeciesDto extends BaseDTO implements HasTagDTO {
    private String linkImageUrl;
    private String parent;
    @JsonProperty(namespace = "properties")
    private CreaturePropertiesDto properties = new CreaturePropertiesDto();
    private Collection<SpeciesCreateFeatureDto> features = new ArrayList<>();
    private Set<String> tags = new HashSet<>();
}
