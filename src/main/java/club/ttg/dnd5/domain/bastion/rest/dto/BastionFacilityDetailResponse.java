package club.ttg.dnd5.domain.bastion.rest.dto;

import club.ttg.dnd5.domain.bastion.model.FacilityChoice;
import club.ttg.dnd5.domain.bastion.model.FacilityEnlargement;
import club.ttg.dnd5.domain.bastion.model.FacilityFeature;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Сооружение бастиона")
public class BastionFacilityDetailResponse extends BaseResponse {
    @Schema(description = "Вид сооружения")
    private BastionLabel category;
    @Schema(description = "Требуемый уровень персонажа")
    private Long level;
    @Schema(description = "Требование помимо уровня")
    private BastionLabel prerequisite;
    @Schema(description = "Пространство")
    private FacilitySpaceResponse space;
    @Schema(description = "Число наёмников")
    private Integer hirelings;
    @Schema(description = "Можно ли иметь больше одного такого сооружения")
    private Boolean repeatable;
    @Schema(description = "Приказы сооружения")
    private List<BastionLabel> orders;
    @Schema(description = "Варианты исполнения приказов")
    private List<FacilityOrderOption> orderOptions;
    @Schema(description = "Расширение сооружения")
    private FacilityEnlargement enlargement;
    @Schema(description = "Постоянные свойства сооружения")
    private List<FacilityFeature> features;
    @Schema(description = "Выборы игрока для сооружения")
    private List<FacilityChoice> choices;
}
