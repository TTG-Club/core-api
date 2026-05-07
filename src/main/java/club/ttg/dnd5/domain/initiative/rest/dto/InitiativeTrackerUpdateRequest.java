package club.ttg.dnd5.domain.initiative.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на обновление трекера инициативы или его настроек")
@Getter
@Setter
public class InitiativeTrackerUpdateRequest {
    @Schema(description = "Название трекера", examples = "Финальная битва")
    private String title;

    @Schema(description = "Перебрасывать инициативу автоматически в начале каждого раунда")
    private Boolean rerollEachRound;

    @Schema(description = "Кидать одну инициативу для одинаковых существ из бестиария")
    private Boolean groupSameCreaturesInitiative;
}
