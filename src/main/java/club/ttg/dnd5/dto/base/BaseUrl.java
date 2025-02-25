package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public abstract class BaseUrl {
    @Schema(description = "unique URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "image")
    @Schema(description = "image URL")
    private String imageUrl;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> gallery = new ArrayList<>();
}
