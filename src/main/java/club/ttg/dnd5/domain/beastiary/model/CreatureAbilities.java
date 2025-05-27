package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureAbilities {
    /**
     * Сила
     */
    @Schema(description = "Сила")
    @JsonProperty("str")
    private CreatureAbility strength;
    /**
     * Ловкость
     */
    @JsonProperty("dex")
    @Schema(description = "Ловкость")
    private CreatureAbility dexterity;
    /**
     * Телосложение
     */
    @Schema(description = "Телосложение")
    @JsonProperty("con")
    private CreatureAbility constitution;
    /**
     * Интеллект
     */
    @Schema(description = "Интеллект")
    @JsonProperty("int")
    private CreatureAbility intelligence;
    /**
     * Мудрость
     */
    @JsonProperty("wis")
    @Schema(description = "Мудрость")
    private CreatureAbility wisdom;
    /**
     * Харизма
     */
    @Schema(description = "Харизма")
    @JsonProperty("chr")
    private CreatureAbility charisma;

    public int getMod(Ability ability) {
        return switch (ability) {
            case STRENGTH -> strength.mod();
            case DEXTERITY -> dexterity.mod();
            case CONSTITUTION -> constitution.mod();
            case INTELLIGENCE -> intelligence.mod();
            case WISDOM -> wisdom.mod();
            case CHARISMA -> charisma.mod();
        };
    }
}
