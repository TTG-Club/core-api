package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@Schema(description = "Информация о происхождении")
public class BackgroundDetailResponse extends BaseDto {
    @Schema(description = "Характеристики:")
    private String abilityScores;
    @Schema(description = "Черта")
    private String feat;
    @Schema(description = "Владение инструментом")
    private String skillProficiencies;
    @Schema(description = "Снаряжение")
    private String toolProficiency;
}
