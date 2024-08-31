package club.ttg.dnd5.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
public class NameDto {
    @Schema(description = "имя по русски", defaultValue = "Бард", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rus;
    @Schema(description = "имя по английски", defaultValue = "Bard", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eng;
    @Schema(description = "альтернативное имя", defaultValue = "<fhl",  requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String alt;
    @Schema(description = "имя на руском в родительном падеже", defaultValue = "барда", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String genitive;
    @Schema(description = "краткое имя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shortName;
}
