package club.ttg.dnd5.domain.bastion.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Значение справочника бастиона для показа: код, который шлёт редактор и хранит мини-игра,
 * и русская подпись.
 */
@Schema(description = "Значение справочника")
public record BastionLabel(
        @Schema(description = "Код значения", examples = {"CRAFT"}) String value,
        @Schema(description = "Подпись", examples = {"Изготовить"}) String name) {
}
