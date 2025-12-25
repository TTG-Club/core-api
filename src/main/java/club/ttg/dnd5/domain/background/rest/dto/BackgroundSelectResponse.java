package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Schema(description = "Предыстория запрос")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BackgroundSelectResponse {
    @Schema(description = "unique URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameRequest name;
    @Schema(description = "Характеристики:", examples = {"STRENGTH", "DEXTERITY"})
    private Set<Ability> abilityScores;
    @Schema(description = "URL черты")
    private String featUrl;
    @Schema(description = "Навыки", examples = {"ACROBATICS", "ATHLETICS"})
    private Set<Skill> skillsProficiencies;
    @JsonProperty(value = "source")
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceRequest source;
}
