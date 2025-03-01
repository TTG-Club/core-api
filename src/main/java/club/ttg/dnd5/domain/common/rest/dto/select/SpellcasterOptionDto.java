package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpellcasterOptionDto extends BaseSelectOptionDto {

    @Schema(
            example = "полный заклинатель",
            description = "Отображаемое имя в выпадающих списках.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
            example = "FULL",
            description = "Используемое значение. Передается в запросах на API, например, при создании вида.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String value;

    @Schema(
            example = "9",
            description = "Количество доступных кругов заклинаний",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int levels;
}
