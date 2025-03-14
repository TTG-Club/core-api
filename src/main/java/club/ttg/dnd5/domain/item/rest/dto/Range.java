package club.ttg.dnd5.domain.item.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Дистанция и дальность")
public class Range {
    @Schema(description = "Нормальная")
    private short normal;
    @Schema(description = "Максимальная")
    private short max;
}
