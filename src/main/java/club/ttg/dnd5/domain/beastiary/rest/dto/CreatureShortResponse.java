package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatureShortResponse extends ShortResponse {
    @Schema(name = "cr", description = "Уровень опасности")
    private String challengeRailing;
    @Schema(description = "Тип (или типы) существ")
    private String type;
}
