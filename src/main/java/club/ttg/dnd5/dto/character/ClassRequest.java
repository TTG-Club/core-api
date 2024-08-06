package club.ttg.dnd5.dto.character;


import club.ttg.dnd5.dto.NameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collection;

@Builder
public class ClassRequest {
    @Schema(description = "назваия", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameDto name;
    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private Collection<ClassFeatureDto> features;
}
