package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Умение вида или происхождения")
public class SpeciesFeatureResponse {
    private String url;
    @Schema(description = "Название вида", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameResponse name;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}
