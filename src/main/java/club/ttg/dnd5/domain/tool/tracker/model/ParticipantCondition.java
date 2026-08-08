package club.ttg.dnd5.domain.tool.tracker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Состояние участника боя (отравлен, лежит ничком и прочее). Хранится в jsonb-колонке участника
 * и тем же объектом уходит в ответ: справочник состояний ведёт фронтенд, сервер знает только
 * ключ и срок.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantCondition {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Ключ состояния из справочника фронтенда (например, poisoned)")
    private String key;

    @Nullable
    @Schema(description = "Раунд, к которому состояние спадает само. NULL — до снятия вручную")
    private Integer expiresAtRound;

    @Nullable
    @Size(max = 20)
    @Schema(description = "Момент, когда состояние спадает: round-end — на границе раунда, "
            + "turn-start — в начале своего хода, turn-end — в конце своего хода. "
            + "NULL — в начале своего хода")
    private String expiresOn;
}
