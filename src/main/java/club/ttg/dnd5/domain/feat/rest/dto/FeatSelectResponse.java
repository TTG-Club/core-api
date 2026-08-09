package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import club.ttg.dnd5.dto.base.SourceResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class FeatSelectResponse{
    @Schema(description = "unique URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameRequest name;
    @Schema(description = "Категория черты", examples = {"ORIGIN", "GENERAL", "EPIC_BOON", "FIGHTING_STYLE"})
    private FeatCategory category;
    @Schema(description = "Предварительное условие")
    private String prerequisite;
    @Schema(description = "Предварительное условие в разобранном виде")
    private FeatPrerequisite prerequisiteDetails;
    @Schema(description = "Повторяемость")
    private Boolean repeatability;
    /** @deprecated плоская проекция {@code mechanics.abilityBonuses[*].abilities}. */
    @Deprecated
    @Schema(description = "Улучшаемые характеристики (устарело, см. mechanics.abilityBonuses)",
            examples = {"STRENGTH", "DEXTERITY", "CONSTITUTION"})
    private Collection<Ability> abilities;
    /** @deprecated наибольшее {@code count} среди {@code mechanics.abilityBonuses}. */
    @Deprecated
    @Schema(description = "Количество улучшаемых характеристик (устарело, см. mechanics.abilityBonuses)")
    private int abilityScoreIncreaseOptions;
    @Schema(description = "Механика влияния черты на лист персонажа")
    private FeatMechanics mechanics;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source;
}
