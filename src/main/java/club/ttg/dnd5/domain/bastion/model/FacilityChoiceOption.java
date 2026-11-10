package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.model.EntityRef;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Вариант выбора сооружения ({@link FacilityChoice}). */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Вариант выбора")
public class FacilityChoiceOption {
    @Schema(description = "Название", examples = {"Травяной"})
    private String name;
    @Schema(description = "Описание или эффект")
    private String description;
    @Schema(description = "Предмет справочника, если варианты берутся из него", example = "smiths-tools-phb")
    private String url;
    @Schema(description = "Что можно создать этим вариантом (предметы инструмента ремесленника)")
    private List<EntityRef> crafts;
}
