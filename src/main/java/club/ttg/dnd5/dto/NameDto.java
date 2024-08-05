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
    @Schema(description = "имя по русски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rus;
    @Schema(description = "имя по английски", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eng;
    @Schema(description = "альтернативное имя")
    private String alt;
}
