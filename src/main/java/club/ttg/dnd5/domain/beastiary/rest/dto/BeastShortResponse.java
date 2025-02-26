package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class BeastShortResponse extends BaseDto {
    @Schema(name = "CR", description = "Уровень опасности")
    private String challengeRailing;
}
