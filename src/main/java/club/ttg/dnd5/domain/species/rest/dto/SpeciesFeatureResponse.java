package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.rest.dto.FeatSpellListGroupResponse;
import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Schema(description = "Умение вида или происхождения")
public class SpeciesFeatureResponse {
    private String url;
    @Schema(description = "Название вида", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameResponse name;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    @Schema(description = "Уровень персонажа, с которого умение действует; null — с первого",
            example = "3")
    private Integer level;
    @Schema(description = "Механика влияния умения на лист персонажа")
    private SpeciesMechanics mechanics;
    @Schema(description = "Заклинания, которые даёт умение; уровень доступа берётся у самого умения")
    private List<GrantedSpellRef> grantedSpells;
    @Schema(description = "Активные эффекты умения в вокабуляре VTTG")
    private List<ActiveEffect> activeEffects;
    @Schema(description = "Расширение списка заклинаний умением с данными справочника")
    private Collection<FeatSpellListGroupResponse> spellListGroups;
}
