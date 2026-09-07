package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.model.SectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentItemDto {
    @Schema(description = "URL предмета", example = "dagger")
    private String url;

    @Schema(description = "Название предмета", example = "Кинжал")
    private String name;

    @Schema(description = "Количество предметов", example = "2")
    private Integer quantity;

    @Schema(description = "Уточнение к предмету", example = "по вашему выбору")
    private String description;

    @Schema(description = "Раздел карточки предмета", example = "items")
    private SectionType section;

    /** Позиция без раздела — стартовое снаряжение предыстории и класса. */
    public EquipmentItemDto(String url, String name, Integer quantity, String description) {
        this(url, name, quantity, description, null);
    }
}
