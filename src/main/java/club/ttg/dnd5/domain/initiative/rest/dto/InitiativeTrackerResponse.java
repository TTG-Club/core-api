package club.ttg.dnd5.domain.initiative.rest.dto;

import club.ttg.dnd5.domain.initiative.model.EncounterDifficulty;
import club.ttg.dnd5.domain.initiative.model.InitiativeTrackerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Состояние трекера инициативы")
@Getter
@Builder
public class InitiativeTrackerResponse {
    @Schema(description = "Идентификатор трекера")
    private UUID id;

    @Schema(description = "Название трекера")
    private String title;

    @Schema(description = "Статус трекера: подготовка, активный бой или завершен")
    private InitiativeTrackerStatus status;

    @Schema(description = "Текущий раунд боя")
    private int currentRound;

    @Schema(description = "Идентификатор участника, чей ход сейчас активен")
    private UUID currentParticipantId;

    @Schema(description = "Перебрасывать инициативу автоматически в начале каждого раунда")
    private boolean rerollEachRound;

    @Schema(description = "Кидать одну инициативу для одинаковых существ из бестиария")
    private boolean groupSameCreaturesInitiative;

    @Schema(description = "Токен публичной ссылки для просмотра трекера")
    private String shareToken;

    @Schema(description = "Расчет сложности боевого столкновения")
    private EncounterDifficulty encounterDifficulty;

    @Schema(description = "Участники трекера в порядке инициативы")
    private List<InitiativeParticipantResponse> participants;

    @Schema(description = "Дата создания трекера")
    private Instant createdAt;

    @Schema(description = "Дата последнего обновления трекера")
    private Instant updatedAt;
}
