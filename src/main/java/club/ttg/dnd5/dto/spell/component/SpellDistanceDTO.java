package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("distance")
public class SpellDistanceDTO {

    @JsonProperty("type")
    private String type; // Тип дистанции (точная, радиус и т.д.)

    @JsonProperty("value")
    private Integer value; // Значение в футах (если применимо)

    @JsonProperty("custom")
    private String custom; // Кастомное значение (например, "в пределах слуха")
}
