package club.ttg.dnd5.domain.character_class.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MulticlassDto {
    @JsonProperty("class")
    private String name;
    private String subclass;
    private int level;
    private String hitDice;
}
