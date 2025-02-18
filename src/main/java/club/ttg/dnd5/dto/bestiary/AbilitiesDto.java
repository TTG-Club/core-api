package club.ttg.dnd5.dto.bestiary;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilitiesDto {
    @Schema(description = "Сила")
    @JsonProperty(value = "STR")
    private AbilityDto strength;
    @Schema(description = "Ловкость")
    @JsonProperty(value = "DEX")
    private AbilityDto dexterity;
    @Schema(description = "Телосложение")
    @JsonProperty(value = "CON")
    private AbilityDto constitution;
    @Schema(description = "Интеллекта")
    @JsonProperty(value = "INT")
    private AbilityDto intelligence;
    @Schema(description = "Мудрость")
    @JsonProperty(value = "WIS")
    private AbilityDto wisdom;
    @Schema(description = "Харизма")
    @JsonProperty(value = "CHA")
    private AbilityDto charisma;
}
