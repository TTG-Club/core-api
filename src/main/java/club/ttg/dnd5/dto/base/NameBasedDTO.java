package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonRootName("name")
@Builder
public class NameBasedDTO implements HasNameResponse {
    @JsonProperty(value = "rus")
    @Schema(description = "русское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "eng")
    @Schema(description = "английское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String english = "";
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "alt")
    @Schema(description = "альтернативные названия")
    private ArrayList<String> alternative = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "short")
    @Schema(description = "краткое название")
    private String shortName = "";
}
