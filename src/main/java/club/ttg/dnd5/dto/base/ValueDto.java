package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@Getter
public class ValueDto {
    NameBasedDTO name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "значение")
    private Object value;
}
