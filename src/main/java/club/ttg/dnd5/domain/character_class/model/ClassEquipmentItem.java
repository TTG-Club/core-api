package club.ttg.dnd5.domain.character_class.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Предмет в варианте стартового снаряжения класса.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassEquipmentItem {
    @Schema(description = "URL предмета", example = "dagger")
    private String url;

    @Schema(description = "Название предмета на момент сохранения", example = "Кинжал")
    private String name;

    @Schema(description = "Количество предметов", example = "2")
    private Integer quantity;

    @Schema(description = "Уточнение к предмету", example = "по вашему выбору")
    private String description;
}
