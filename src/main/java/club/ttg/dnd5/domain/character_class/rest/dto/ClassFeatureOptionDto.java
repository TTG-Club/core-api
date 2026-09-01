package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.GrantedSpellResponse;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureOptionDto {

    @Schema(description = "Stable option slug", example = "agonizing_blast")
    private String key;

    @Schema(description = "Option name")
    private Name name;

    @Schema(description = "Option description")
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;

    @Schema(description = "Short additional label")
    private String additional;

    @Schema(description = "Option prerequisite")
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String prerequisite;

    @Schema(description = "Required class level for this option")
    private Integer requiredClassLevel;

    @Schema(description = "Hide option in subclass and multiclass contexts")
    private boolean hideInSubclasses;

    @Schema(description = "Вариант можно выбрать повторно на следующей ступени выбора")
    private boolean repeatable;

    @Schema(description = "Механика влияния варианта на лист персонажа")
    private ClassMechanics mechanics;

    @Schema(description = "Активные эффекты варианта в вокабуляре VTTG")
    private List<ActiveEffect> activeEffects;

    /**
     * Заклинания, которые вариант выдаёт без выбора, — записями справочника.
     *
     * <p>Подставляются так же, как у умения ({@code ClassFeatureDto.grantedSpells}): в
     * механике лежат ссылки, а листу персонажа нужен круг заклинания, чтобы положить его
     * в книгу.</p>
     */
    @Schema(description = "Заклинания, выдаваемые вариантом, с данными справочника")
    private List<GrantedSpellResponse> grantedSpells;

    public ClassFeatureOptionDto(ClassFeatureOption option) {
        this.key = option.getKey();
        this.name = option.getName();
        this.description = option.getDescription();
        this.additional = option.getAdditional();
        this.prerequisite = option.getPrerequisite();
        this.requiredClassLevel = option.getRequiredClassLevel();
        this.hideInSubclasses = option.isHideInSubclasses();
        this.repeatable = option.isRepeatable();
        this.mechanics = option.getMechanics();
        this.activeEffects = option.getActiveEffects();
    }
}
