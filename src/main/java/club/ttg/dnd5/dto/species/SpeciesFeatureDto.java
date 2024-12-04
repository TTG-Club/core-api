package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasTagDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesFeatureDto extends BaseDTO implements HasTagDTO {
    @JsonProperty("tags")
    private Set<String> tags = new HashSet<>();
}