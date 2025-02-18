package club.ttg.dnd5.dto.bestiary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilityDto {
    private byte value;
    @JsonProperty(value = "MOD")
    private byte modifier;
    @JsonProperty(value = "SAVE")
    private byte save;
}
