package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Активный участник трекера и его статблок")
@Getter
@Builder
public class ActiveParticipantResponse {
    @Schema(description = "Участник, чей ход сейчас активен")
    private InitiativeParticipantResponse participant;

    @Schema(description = "Статблок существа из бестиария, если активный участник является существом")
    private CreatureDetailResponse statBlock;
}
