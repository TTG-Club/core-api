package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Name {
    @JsonProperty(value = "rus")
    @Schema(description = "русское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name = "";
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "eng")
    @Schema(description = "английское название", requiredMode = Schema.RequiredMode.REQUIRED)
    private String english = "";
}
