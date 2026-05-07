package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.InitiativeParticipantState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на ручное изменение состояния участника")
@Getter
@Setter
public class ParticipantStateRequest {
    @Schema(description = "Новое состояние участника")
    private InitiativeParticipantState state;
}
