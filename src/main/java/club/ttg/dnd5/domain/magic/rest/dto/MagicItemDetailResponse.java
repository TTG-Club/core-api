package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemDetailResponse extends BaseResponse {
    @Schema(description = "Подзаголовок")
    private String subtitle;

    @Schema(description = "Механика влияния на лист персонажа; null — предмет её не описывает")
    private MagicItemMechanics mechanics;
}
