package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiceOptionDto extends BaseSelectOptionDto {

    @Schema(
            example = "к20",
            description = "Отображаемое имя в выпадающих списках.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
            example = "d20",
            description = "Используемое значение. Передается в запросах на API, например, при создании вида.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String value;

    @Schema(
            example = "9",
            description = "Максимальное значение на кубе",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int maxValue;
}
