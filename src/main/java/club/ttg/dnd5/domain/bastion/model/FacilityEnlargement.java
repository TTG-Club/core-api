package club.ttg.dnd5.domain.bastion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Расширение специализированного сооружения, если его описание это допускает. */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Расширение сооружения")
public class FacilityEnlargement {
    @Schema(description = "Пространство после расширения")
    private FacilitySpace space;
    @Schema(description = "Стоимость расширения, зм")
    private Integer cost;
    @Schema(description = "Время расширения, дни; null — в описании не указано")
    private Integer days;
    @Schema(description = "Сколько наёмников добавляется при расширении")
    private Integer additionalHirelings;
    @Schema(description = "Что даёт расширение")
    private String description;
}
