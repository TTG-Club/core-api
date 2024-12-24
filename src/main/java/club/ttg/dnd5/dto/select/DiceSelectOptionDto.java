package club.ttg.dnd5.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DiceSelectOptionDto extends SelectOptionDto {
    @Schema(
            example = "9",
            description = "Максимальное значение на кубе",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int maxValue;
}
