package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilityResponse {
    @Schema(description = "Значение характеристики")
    private short value;
    @Schema(description = "Модификатор характеристики")
    private String mod;
    @Schema(description = "Бонус к спасброску")
    private String sav;
}
