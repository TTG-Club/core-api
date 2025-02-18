package club.ttg.dnd5.dto.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Schema(description = "Хиты")
public class HitPointDto {
    @Schema(description = "Среднее значение")
    private String average;
    @Schema(description = "Формула")
    private String formula;
}
