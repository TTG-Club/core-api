package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemDetailResponse extends BaseResponse {
    @Schema(description = "Подзаголовок")
    private String subtitle;
}
