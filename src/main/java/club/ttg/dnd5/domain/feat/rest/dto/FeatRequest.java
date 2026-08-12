package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class FeatRequest extends BaseRequest {
    @Schema(description = "Категория черты", examples = {"ORIGIN", "GENERAL", "EPIC_BOON", "FIGHTING_STYLE"})
    private FeatCategory category;
    @Schema(description = "Предварительное условие как в книге")
    private String prerequisite;
    @Schema(description = "Предварительное условие в разобранном виде")
    private FeatPrerequisite prerequisiteDetails;
    @Schema(description = "Повторяемость")
    private Boolean repeatability;
    /**
     * Не редактируется: пересобирается из {@code mechanics.abilityBonuses} при сохранении.
     * Поле остаётся в форме, чтобы читались снимки ревизий, снятые до появления механики.
     */
    @Deprecated
    @Schema(description = "Улучшаемые характеристики (устарело, см. mechanics.abilityBonuses)",
            examples = {"STRENGTH", "DEXTERITY", "CONSTITUTION"})
    private Collection<Ability> abilities;
    @Schema(description = "Механика влияния черты на лист персонажа")
    private FeatMechanics mechanics;
}
