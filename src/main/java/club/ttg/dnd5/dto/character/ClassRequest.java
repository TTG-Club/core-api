package club.ttg.dnd5.dto.character;


import club.ttg.dnd5.dto.NameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Builder
@Getter
@Setter
public class ClassRequest {
    @Schema(description = "названия", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameDto name;
    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private Collection<ClassFeatureDto> features;
    @Schema(description = "описание класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentUrl;
}
