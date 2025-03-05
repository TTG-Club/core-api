package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MeasurableSelectOptionDto extends SelectOptionDto {
    @Schema(example = "true",
            description = "Возможность задать единицы измерения")
    private Boolean measurable;
}
