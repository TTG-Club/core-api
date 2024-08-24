package club.ttg.dnd5.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@Getter
public class ValueDto {
    @Schema(description = "имя по русски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rus;
    @Schema(description = "имя по английски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eng;
    @Schema(description = "значение")
    private Object value;
}
