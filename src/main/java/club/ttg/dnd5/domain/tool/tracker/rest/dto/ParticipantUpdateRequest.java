package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantCondition;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantSheetLink;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Nullable
    @PositiveOrZero
    @Schema(description = "Текущие хиты участника")
    private Integer currentHitPoints;

    @Nullable
    @Positive
    @Schema(description = "Максимум хитов: прокинутая формула статблока или значение с листа персонажа")
    private Integer maxHitPoints;

    @Nullable
    @Min(1)
    @Max(50)
    @Schema(description = "Класс доспеха игрока (существу берётся из статблока)")
    private Integer armorClass;

    @Nullable
    @Size(max = 20)
    @Schema(description = "Цвет иконки участника, когда картинки нет")
    private String color;

    @Nullable
    @Valid
    @Schema(description = "Привязка игрока к листу персонажа")
    private ParticipantSheetLink sheetLink;

    @Nullable
    @Valid
    @Schema(description = "Наложенные состояния целиком: присланный список заменяет прежний, "
            + "пустой — снимает все")
    private List<ParticipantCondition> conditions;
}
