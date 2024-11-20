package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@JsonRootName("name")
@Builder
public class NameBasedDTO implements HasNameResponse {
    @JsonProperty(value = "rus")
    private String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "eng")
    private String english = "";
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "alt")
    private String alternative = "";
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "short")
    private String shortName = "";
}
