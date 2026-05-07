package club.ttg.dnd5.domain.initiative.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Запрос на создание трекера инициативы")
@Getter
@Setter
public class InitiativeTrackerCreateRequest {
    @Schema(description = "Название трекера", examples = "Засада у старой мельницы")
    private String title;

    @Schema(description = "Перебрасывать инициативу автоматически в начале каждого раунда")
    private boolean rerollEachRound;

    @Schema(description = "Кидать одну инициативу для одинаковых существ из бестиария")
    private boolean groupSameCreaturesInitiative;
}
