package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Schema(description = "Виды и происхождения")
public class SpeciesDetailResponse extends BaseDto {
    @JsonProperty(value = "properties")
    private SpeciesPropertiesDto properties = new SpeciesPropertiesDto();
    private String linkImageUrl;
    // Связанные сущности
    @JsonProperty(value = "species")
    private SpeciesDetailResponse parent;
    /**
     * Происхождения.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Происхождения")
    private Collection<SpeciesDetailResponse> lineages = new LinkedHashSet<>();

    @Schema(description = "Умения")
    private Collection<SpeciesFeatureResponse> features;
    private NameDto group = new NameDto();
}

