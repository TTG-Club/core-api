package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Collection;
import java.util.Set;

@Getter
public class BackgroundRequest extends BaseDto {
    @Schema(description = "Характеристики:")
    private Collection<String> abilityScores;
    @Schema(description = "Черта")
    private String featUrl;
    @Schema(description = "Владение инструментом")
    private Set<Skill> skillProficiencies;
    @Schema(description = "Снаряжение")
    private String toolProficiency;
}
