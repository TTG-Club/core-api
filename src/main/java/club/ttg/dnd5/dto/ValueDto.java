package club.ttg.dnd5.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public class ValueDto {
    @Schema(description = "имя по русски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rus;
    @Schema(description = "имя по английски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eng;
    @Schema(description = "значение")
    private Object value;
}
