package club.ttg.dnd5.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Описывает один пункт в выпадающем списке.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectOptionDto extends BaseSelectOptionDto {

    @Schema(
            example = "Средний",
            description = "Отображаемое имя в выпадающих списках.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
            example = "MEDIUM",
            description = "Используемое значение. Передается в запросах на API, например, при создании вида.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String value;
}
