package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantCondition;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantSheetLink;
import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantResponse {

    @NotNull
    @Schema(description = "Идентификатор участника")
    private UUID id;

    @NotNull
    @Schema(description = "Тип участника (ключ): PLAYER или CREATURE")
    private ParticipantType type;

    @NotNull
    @Schema(description = "Человеко-читаемый тип: «Игрок» или «Существо»")
    private String typeName;

    @NotNull
    @Schema(description = "Имя участника")
    private String name;

    @Schema(description = "Бонус инициативы")
    private int initiativeBonus;

    @Schema(description = "Повержен: остаётся в списке, но пропускается в порядке хода")
    private boolean dead;

    @Nullable
    @Schema(description = "Результат броска d20. NULL — инициатива ещё не брошена")
    private Integer initiativeRoll;

    @Nullable
    @Schema(description = "Итог инициативы: бросок + бонус. NULL — инициатива ещё не брошена")
    private Integer initiativeTotal;

    @Nullable
    @Schema(description = "Слаг существа в бестиарии (для перехода к статблоку). NULL — игрок")
    private String creatureUrl;

    @Nullable
    @Schema(description = "Текущие хиты. NULL — мастер их не вёл")
    private Integer currentHitPoints;

    @Nullable
    @Schema(description = "Максимум хитов, заданный мастером. NULL — берётся среднее из статблока")
    private Integer maxHitPoints;

    @Nullable
    @Schema(description = "Класс доспеха игрока. NULL — не задан либо это существо")
    private Integer armorClass;

    @Nullable
    @Schema(description = "Цвет иконки участника. NULL — цвет по умолчанию")
    private String color;

    @Nullable
    @Schema(description = "Привязка игрока к листу персонажа. NULL — заведён вручную")
    private ParticipantSheetLink sheetLink;

    @Nullable
    @Schema(description = "Наложенные состояния. NULL — состояний нет")
    private List<ParticipantCondition> conditions;
}
