package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Schema(description = "Предыстория запрос")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BackgroundRequest extends BaseRequest {
    @Schema(description = "Характеристики:", examples = {"STRENGTH", "DEXTERITY"})
    private Set<Ability> abilityScores;
    @Schema(description = "URL черты")
    private String featUrl;
    @Schema(description = "Навыки", examples = {"ACROBATICS", "ATHLETICS"})
    private Set<Skill> skillsProficiencies;
    @Schema(description = "Владение инструментами")
    private String toolProficiency;
    @Schema(description = "Снаряжение")
    private String equipment;
}
