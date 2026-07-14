package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackerDetailedResponse {

    @NotNull
    @Schema(description = "Идентификатор трекера")
    private UUID id;

    @NotNull
    @Schema(description = "Название трекера")
    private String name;

    @NotNull
    @Schema(description = "Статус (ключ): PREPARING или ACTIVE")
    private TrackerStatus status;

    @NotNull
    @Schema(description = "Человеко-читаемый статус: «Подготовка» или «Бой»")
    private String statusName;

    @Schema(description = "Номер раунда боя: 0 — бой не начат")
    private int round;

    @Schema(description = "Новая инициатива каждый раунд (опция)")
    private boolean rerollEachRound;

    @Nullable
    @Schema(description = "id участника, чей сейчас ход. NULL — бой не начат")
    private UUID currentParticipantId;

    @Nullable
    @Schema(description = "Секретный ключ доступа к анонимному трекеру: сохраните его на клиенте "
            + "и передавайте в заголовке X-Tracker-Key. Для трекера с владельцем не используется")
    private UUID accessKey;

    @Nullable
    @Schema(description = "Дата создания")
    private Instant createdAt;

    @Nullable
    @Schema(description = "Дата последнего изменения")
    private Instant updatedAt;

    @NotNull
    @Schema(description = "Участники в порядке хода; не бросавшие инициативу — в конце, в порядке добавления")
    private List<ParticipantResponse> participants;
}
