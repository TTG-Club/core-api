package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CreaturePropertiesDto {
    private Size size;
    private CreatureType type;
    @JsonProperty(value = "speed")
    MovementAttributes movementAttributes;
    private int darkVision;
}