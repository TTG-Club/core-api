package club.ttg.dnd5.domain.common.rest.dto.select;

import club.ttg.dnd5.dto.base.SelectableEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Абстрактный класс одного пункта в выпадающем списке.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseSelectOptionDto {

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
    private Object value;

    public BaseSelectOptionDto(SelectableEnum selectableEnum) {
        this.label = selectableEnum.getLabel();
        this.value = selectableEnum.getValue();
    }
}
