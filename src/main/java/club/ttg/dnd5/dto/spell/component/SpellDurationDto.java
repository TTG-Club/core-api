package club.ttg.dnd5.dto.spell.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("duration")
public class SpellDurationDto {

    @JsonProperty("value")
    private Integer value; // Значение (например, 10 минут)

    @JsonProperty("type")
    private String type; // Тип (минута, час, день)

    @JsonProperty("custom")
    private String custom; // Кастомное значение (например, "Пока не исчезнет луна")

    @JsonProperty("concentration")
    private boolean concentration; // Требуется ли концентрация
}
