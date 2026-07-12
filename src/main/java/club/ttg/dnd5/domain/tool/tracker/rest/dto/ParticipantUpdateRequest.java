package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Правка участника: применяются только заполненные поля, null — «не менять».
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantUpdateRequest {

    @Nullable
    @Size(max = 100)
    @Schema(description = "Новое имя участника")
    private String name;

    @Nullable
    @Min(-20)
    @Max(30)
    @Schema(description = "Новый бонус инициативы (итог пересчитается, если бросок уже сделан)")
    private Integer initiativeBonus;

    @Nullable
    @Min(1)
    @Max(20)
    @Schema(description = "Ручной результат броска d20 — если игрок кидает живые кости, мастер вносит "
            + "выпавшее значение; итог считается как бросок + бонус")
    private Integer initiativeRoll;

    @Nullable
    @Schema(description = "Пометить участника мёртвым/живым: true — повержен (остаётся в списке, "
            + "но пропускается в порядке хода), false — вернуть в бой")
    private Boolean dead;
}
