package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SavedCharacterSheetHitPointsRequest {

    @NotNull
    @PositiveOrZero
    @Schema(description = "Текущие хиты персонажа. Больше максимума листа — запишется максимум")
    private Integer current;
}
