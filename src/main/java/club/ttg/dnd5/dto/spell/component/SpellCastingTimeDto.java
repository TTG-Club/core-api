package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("casting_time")
public class SpellCastingTimeDto {

    @JsonProperty("value")
    private Integer value; // Значение (например, 1 действие)

    @JsonProperty("type")
    private String type; // Тип (мгновенно, действие, минута)

    @JsonProperty("custom")
    private String custom; // Кастомное значение (например, "С первыми лучами солнца")

    @JsonProperty("ritual")
    private boolean ritual; // Может ли быть ритуалом
}
