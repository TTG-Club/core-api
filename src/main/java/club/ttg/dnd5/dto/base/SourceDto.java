package club.ttg.dnd5.dto.base;

import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import club.ttg.dnd5.domain.common.rest.dto.NameValueDto;
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
    @Schema(description = "имя источника")
    private NameDto name;
    private NameValueDto group;
    @Schema(description = "страница источника", example = "99")
    private Short page;
}
