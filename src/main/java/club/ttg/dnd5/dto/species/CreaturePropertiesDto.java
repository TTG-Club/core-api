package club.ttg.dnd5.dto.species;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonRootName(value = "properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreaturePropertiesDto {
    @JsonProperty(value = "speed")
    MovementAttributes movementAttributes = new MovementAttributes();
    private List<String> sizes;
    private String type;
    private int darkVision;
}