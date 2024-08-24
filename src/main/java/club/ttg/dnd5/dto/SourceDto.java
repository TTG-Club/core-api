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
public class SourceDto {
    @Schema(description = "краткое имя источника", requiredMode = Schema.RequiredMode.REQUIRED, example = "PHB")
    private String shortName;
    @Schema(description = "имя источника", requiredMode = Schema.RequiredMode.REQUIRED, example = "Книга игрока")
    private String name;
    private NameValueDto group;
    @Schema(description = "страницв источника", example = "99")
    private Short page;
}
