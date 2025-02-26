package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dto.BaseDto;
import club.ttg.dnd5.dto.base.HasTagDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SpeciesFeatureDto extends BaseDto implements HasTagDTO {
    @JsonProperty("tags")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> tags = new HashSet<>();
}
