package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilitiesResponse {
    @Schema(description = "Сила")
    private AbilityResponse strength;
    @Schema(description = "Ловкость")
    private AbilityResponse dexterity;
    @Schema(description = "Телосложение")
    private AbilityResponse constitution;
    @Schema(description = "Интеллект")
    private AbilityResponse intelligence;
    @Schema(description = "Мудрость")
    private AbilityResponse wisdom;
    @Schema(description = "Харизма}")
    private AbilityResponse charisma;
}
