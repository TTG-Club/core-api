package club.ttg.dnd5.domain.bastion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
