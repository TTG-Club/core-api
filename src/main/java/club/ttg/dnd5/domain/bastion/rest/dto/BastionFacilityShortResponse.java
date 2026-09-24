package club.ttg.dnd5.domain.bastion.rest.dto;

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
}
