package club.ttg.dnd5.domain.character_class.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MulticlassInfo {
    @JsonProperty("class")
    private String name;
    private String subclass;
    private int level;
    private String hitDice;
}
