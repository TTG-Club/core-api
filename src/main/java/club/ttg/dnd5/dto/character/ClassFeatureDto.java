package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Setter;

import java.util.Collection;

@Builder
@Setter
public class ClassFeatureDto {
    private String url;
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameDto name;
    @Schema(description = "С какого уровня доступно", requiredMode = Schema.RequiredMode.REQUIRED)
    private int level;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private Collection<String> description;
}
