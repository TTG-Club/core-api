package club.ttg.dnd5.dto.base;

import club.ttg.dnd5.domain.common.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@Getter
public class ValueDto {
    NameDto name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "значение")
    private Object value;
}
