package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@JsonRootName("name")
public class NameRequest extends Name {
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "alt")
    @Schema(description = "альтернативные названия")
    private Collection<String> alternative;
    @Schema(description = "Короткая метка")
    private String label;
}
