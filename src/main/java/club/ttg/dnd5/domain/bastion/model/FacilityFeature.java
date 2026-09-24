package club.ttg.dnd5.domain.bastion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Постоянное свойство сооружения, не связанное с приказом: чары после продолжительного
 * отдыха, вдохновение после короткого, восстановление ячейки и т.п.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Свойство сооружения")
public class FacilityFeature {
    @Schema(description = "Название", examples = {"Чары кабинета заклинателя"})
    private String name;
    @Schema(description = "Описание")
    private String description;
}
