package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.dto.base.source.SourceResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureRequest {
    private String name;
    private String description;
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source = new SourceResponse();
}
