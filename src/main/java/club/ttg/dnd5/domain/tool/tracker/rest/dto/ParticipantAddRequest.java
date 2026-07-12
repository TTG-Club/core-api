package club.ttg.dnd5.domain.tool.tracker.rest.dto;

import club.ttg.dnd5.domain.tool.tracker.model.ParticipantType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantAddRequest {

    @NotNull
    @Schema(description = "Тип участника: PLAYER (игрок) или CREATURE (существо из бестиария)")
    private ParticipantType type;

    @Nullable
    @Size(max = 100)
    @Schema(description = "Имя: для игрока обязательно; для существа — переопределяет название из бестиария")
    private String name;

    @Nullable
    @Min(-20)
    @Max(30)
    @Schema(description = "Бонус инициативы игрока (по умолчанию 0). Для существа не учитывается — "
            + "берётся из статблока бестиария")
    private Integer initiativeBonus;

    @Nullable
    @Schema(description = "Слаг существа из бестиария (обязателен для type=CREATURE)")
    private String creatureUrl;

    @Nullable
    @Min(1)
    @Max(100)
    @Schema(description = "Сколько существ добавить одной пачкой (только для CREATURE, по умолчанию 1). "
            + "Имена нумеруются автоматически: «Гоблин 1», «Гоблин 2», ...")
    private Integer count;
}
