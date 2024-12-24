package club.ttg.dnd5.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SpellcasterSelectOptionDto extends SelectOptionDto {
    @Schema(
            example = "9",
            description = "Количество доступных кругов заклинаний",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int levels;
}
