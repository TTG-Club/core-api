package club.ttg.dnd5.domain.beastiary.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastAbilities {
    /**
     * Сила
     */
    @Schema(description = "Сила")
    @JsonProperty("str")
    private BeastAbility strength;
    /**
     * Ловкость
     */
    @JsonProperty("dex")
    @Schema(description = "Ловкость")
    private BeastAbility dexterity;
    /**
     * Телосложение
     */
    @Schema(description = "Телосложение")
    @JsonProperty("con")
    private BeastAbility constitution;
    /**
     * Интеллект
     */
    @Schema(description = "Интеллект")
    @JsonProperty("int")
    private BeastAbility intelligence;
    /**
     * Мудрость
     */
    @JsonProperty("wis")
    @Schema(description = "Мудрость")
    private BeastAbility wisdom;
    /**
     * Харизма
     */
    @Schema(description = "Харизма")
    @JsonProperty("chr")
    private BeastAbility charisma;
}
