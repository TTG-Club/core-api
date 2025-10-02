package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmorOptionDto extends BaseSelectOptionDto {

    @Schema(
            example = "Легкий доспех",
            description = "Отображаемое имя в выпадающих списках.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
            example = "LIGHT",
            description = "Используемое значение. Передается в запросах на API, например, при создании класса.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String value;

    @Schema(
            example = "1 минута",
            description = "Время на то, чтобы надеть доспех",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String putting;

    @Schema(
            example = "1 минута",
            description = "Время на то, чтобы снять доспех",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String removal;
}
