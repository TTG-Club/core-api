package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonRootName("name")
@Builder
public class NameResponse {
    @JsonProperty(value = "rus")
    @Schema(description = "русское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name = "";
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "eng")
    @Schema(description = "английское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String english = "";
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Короткая метка")
    private String label;
}
