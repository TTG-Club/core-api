package club.ttg.dnd5.domain.initiative.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на прямое изменение значений хитов участника")
@Getter
@Setter
public class HpUpdateRequest {
    @Schema(description = "Новое максимальное значение хитов", examples = "42")
    private Integer hpMax;

    @Schema(description = "Новое текущее значение хитов", examples = "31")
    private Integer hpCurrent;

    @Schema(description = "Новое значение временных хитов", examples = "5")
    private Integer hpTemporary;
}
