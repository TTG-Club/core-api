package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BeastShortResponse extends BaseDto {
    @Schema(name = "CR", description = "Уровень опасности")
    private String challengeRailing;
    @Schema(description = "Тип (или типы) существ")
    private String type;
}
