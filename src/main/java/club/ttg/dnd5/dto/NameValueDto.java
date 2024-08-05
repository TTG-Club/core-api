package club.ttg.dnd5.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NameValueDto {
    @Schema(description = "ключ свойства", requiredMode = Schema.RequiredMode.REQUIRED, example = "STRENGTH")
    private Object key;
    @Schema(description = "название свойства")
    private String name;
    @Schema(description = "краткое название свойства")
    private String shortName;
    @Schema(description = "значение свойства", example = "2")
    private Object value;
    @Schema(description = "дополнительный бонус если есть")
    private Object additional;
}
