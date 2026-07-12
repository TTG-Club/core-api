package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
