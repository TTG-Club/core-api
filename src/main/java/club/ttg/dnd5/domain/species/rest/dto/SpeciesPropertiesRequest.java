package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonRootName(value = "properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesPropertiesRequest {
    @JsonProperty(value = "speed")
    private MovementAttributes movementAttributes = new MovementAttributes();
    @Schema(description = "Размеры")
    private Collection<SpeciesSizeDto> sizes;
    @Schema(description = "Тип существа")
    private BeastType type;
}