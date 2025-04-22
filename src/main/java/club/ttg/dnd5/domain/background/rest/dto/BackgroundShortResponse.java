package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Информация о предыстории кратко")
@Getter
@Setter
public class BackgroundShortResponse extends ShortResponse {
    @Schema(description = "Характеристики:")
    private String abilityScores;
}
