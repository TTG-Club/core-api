package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Schema(description = "Виды и происхождения")
public class SpeciesDetailResponse extends BaseResponse {
    @JsonProperty(value = "properties")
    private SpeciesPropertiesResponse properties = new SpeciesPropertiesResponse();
    // Связанные сущности
    @JsonProperty(value = "species")
    private SpeciesDetailResponse parent;

    @Schema(description = "Умения")
    private Collection<SpeciesFeatureResponse> features;
    @Schema(description = "Ссылки на изображения")
    private boolean hasLineages;
}

