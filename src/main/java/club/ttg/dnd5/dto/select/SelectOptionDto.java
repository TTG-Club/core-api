package club.ttg.dnd5.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Описывает один пункт в выпадающем списке.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SelectOptionDto {
    @Schema(
            example = "Средний",
            description = "Отображаемое имя. В выпадающих списках используется для человекочитаемого текста.",
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
