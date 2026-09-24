package club.ttg.dnd5.domain.bastion.rest.dto;

import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Сооружение бастиона в списке")
public class BastionFacilityShortResponse extends ShortResponse {
    @Schema(description = "Вид сооружения")
    private BastionLabel category;
    @Schema(description = "Требуемый уровень персонажа")
    private Long level;
    @Schema(description = "Требование помимо уровня")
    private BastionLabel prerequisite;
    @Schema(description = "Пространство")
    private FacilitySpaceResponse space;
    @Schema(description = "Приказы сооружения")
    private List<BastionLabel> orders;
    /**
     * Можно ли взять сооружение несколько раз и какие выборы у него есть — нужны экрану
     * стартового выбора сооружений в бастионе игроков, чтобы не запрашивать каждое
     * сооружение отдельно.
     */
    @Schema(description = "Можно ли иметь больше одного такого сооружения")
    private Boolean repeatable;
    @Schema(description = "Выборы игрока для сооружения")
    private List<FacilityChoice> choices;
}
