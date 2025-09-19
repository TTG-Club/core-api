package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Abilities {
    /**
     * Сила
     */
    @Schema(description = "Сила")
    @JsonProperty("str")
    private CharAbility strength;
    /**
     * Ловкость
     */
    @JsonProperty("dex")
    @Schema(description = "Ловкость")
    private CharAbility dexterity;
    /**
     * Телосложение
     */
    @Schema(description = "Телосложение")
    @JsonProperty("con")
    private CharAbility constitution;
    /**
     * Интеллект
     */
    @Schema(description = "Интеллект")
    @JsonProperty("int")
    private CharAbility intelligence;
    /**
     * Мудрость
     */
    @JsonProperty("wis")
    @Schema(description = "Мудрость")
    private CharAbility wisdom;
    /**
     * Харизма
     */
    @Schema(description = "Харизма")
    @JsonProperty("chr")
    private CharAbility charisma;

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
