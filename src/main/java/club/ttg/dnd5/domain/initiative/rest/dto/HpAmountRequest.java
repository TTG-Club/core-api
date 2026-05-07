package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.TemporaryHpMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на изменение хитов на указанное значение")
@Getter
@Setter
public class HpAmountRequest {
    @Schema(description = "Количество хитов для урона, лечения или временных хитов", examples = "12")
    private int amount;

    @Schema(description = "Режим применения временных хитов: взять максимум, заменить или очистить")
    private TemporaryHpMode mode = TemporaryHpMode.MAX;
}
