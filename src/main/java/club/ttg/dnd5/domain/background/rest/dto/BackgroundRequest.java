package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BackgroundRequest extends BaseDto {
    @Schema(description = "Характеристики:", examples = {"STRENGTH", "DEXTERITY"})
    private Collection<Ability> abilityScores;
    @Schema(description = "URL черты")
    private String featUrl;
    @Schema(description = "Навыки", examples = {"ACROBATICS", "ATHLETICS"})
    private Set<Skill> skillProficiencies;
    @Schema(description = "Владение инструментами")
    private String toolProficiency;
    @Schema(description = "Снаряжение")
    private String equipment;
}
