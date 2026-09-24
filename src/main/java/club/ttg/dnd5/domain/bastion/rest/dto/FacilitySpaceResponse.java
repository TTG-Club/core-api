package club.ttg.dnd5.domain.bastion.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Пространство сооружения")
public record FacilitySpaceResponse(
        @Schema(description = "Код", examples = {"ROOMY"}) String value,
        @Schema(description = "Подпись", examples = {"Вместительное"}) String name,
        @Schema(description = "Максимальная площадь, квадратов 5×5 футов", examples = {"16"}) int squares) {
}
