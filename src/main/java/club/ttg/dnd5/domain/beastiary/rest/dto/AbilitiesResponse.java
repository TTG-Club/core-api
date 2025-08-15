package club.ttg.dnd5.domain.beastiary.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilitiesResponse {
    @Schema(description = "Сила")
    @JsonProperty("str")
    private AbilityResponse strength;
    @Schema(description = "Ловкость")
    @JsonProperty("dex")
    private AbilityResponse dexterity;
    @Schema(description = "Телосложение")
    @JsonProperty("con")
    private AbilityResponse constitution;
    @Schema(description = "Интеллект")
    @JsonProperty("int")
    private AbilityResponse intelligence;
    @Schema(description = "Мудрость")
    @JsonProperty("wis")
    private AbilityResponse wisdom;
    @Schema(description = "Харизма}")
    @JsonProperty("chr")
    private AbilityResponse charisma;
}
