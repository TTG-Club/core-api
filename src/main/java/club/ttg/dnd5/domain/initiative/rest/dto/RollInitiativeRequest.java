package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.InitiativeRollMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на бросок или ручную установку инициативы участника")
@Getter
@Setter
public class RollInitiativeRequest {
    @Schema(description = "Режим броска инициативы: ручной, обычный, с преимуществом или с помехой")
    private InitiativeRollMode rollMode;

    @Schema(description = "Ручное значение броска d20 для режима MANUAL", examples = "16")
    private Integer rollValue;
}
