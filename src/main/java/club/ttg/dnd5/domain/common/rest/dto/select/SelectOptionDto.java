package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Описывает один пункт в выпадающем списке.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
