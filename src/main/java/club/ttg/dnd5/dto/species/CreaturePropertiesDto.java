package club.ttg.dnd5.dto.species;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreaturePropertiesDto {
    @JsonProperty(value = "speed")
    MovementAttributes movementAttributes;
    private String size;
    private String type;
    private int darkVision;
}