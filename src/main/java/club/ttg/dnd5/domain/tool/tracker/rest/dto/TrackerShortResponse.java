package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.TrackerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrackerShortResponse {

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

    @Schema(description = "Трекер удалён (виден только в истории при includeDeleted=true)")
    private boolean deleted;

    @Nullable
    @Schema(description = "Дата создания (история создания трекеров)")
    private Instant createdAt;

    @Nullable
    @Schema(description = "Дата последнего изменения")
    private Instant updatedAt;
}
