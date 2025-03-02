package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Schema(description = "Вид")
public class SpeciesRequest extends BaseRequest {
    private SpeciesPropertiesDto properties;

    @Schema(description = "Умения")
    private Collection<FeatureRequest> features;
    @Schema(description = "URL на вид", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentUrl;
}
