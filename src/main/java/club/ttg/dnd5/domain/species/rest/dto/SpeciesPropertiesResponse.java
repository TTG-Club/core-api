package club.ttg.dnd5.domain.species.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonRootName(value = "properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesPropertiesResponse {
    @JsonProperty(value = "speed")
    private String speed;
    @Schema(description = "Размеры")
    private String size;
    @Schema(description = "Тип существа")
    private String type;
}