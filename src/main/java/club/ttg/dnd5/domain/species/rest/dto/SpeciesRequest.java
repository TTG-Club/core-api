package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Schema(description = "Вид")
public class SpeciesRequest extends BaseRequest {
    private SpeciesPropertiesRequest properties;

    @Schema(description = "Умения")
    private Collection<FeatureRequest> features;

    @Schema(description = "Механика влияния вида или происхождения на лист персонажа")
    private SpeciesMechanics mechanics;

    @Schema(description = "Врождённые заклинания и уровни их доступности")
    private Collection<SpeciesInnateSpellRequest> innateSpells;
    @Schema(description = "URL на вид", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parent;

    @JsonProperty("linkImage")
    private String linkImageUrl;

}
