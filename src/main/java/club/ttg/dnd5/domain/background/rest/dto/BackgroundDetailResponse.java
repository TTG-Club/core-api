package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@Schema(description = "Информация о происхождении")
public class BackgroundDetailResponse extends BaseResponse {
    @Schema(description = "Характеристики:")
    private String abilityScores;
    @Schema(description = "Название черты")
    private String feat;
    @Schema(description = "Владение инструментом")
    private String skillProficiencies;
    @Schema(description = "Снаряжение")
    private String toolProficiency;
}
