package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BeastShortResponse extends BaseResponse {
    @Schema(name = "CR", description = "Уровень опасности")
    private String challengeRailing;
    @Schema(description = "Тип (или типы) существ")
    private String type;
}
