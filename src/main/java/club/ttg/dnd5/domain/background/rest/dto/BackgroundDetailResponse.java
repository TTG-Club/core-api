package club.ttg.dnd5.domain.background.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Schema(description = "Информация о предыстории")
@Getter
@Setter
public class BackgroundDetailResponse extends BaseResponse {
    @Schema(description = "Характеристики:")
    private String abilityScores;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Название черты")
    private String feat;
    @Schema(description = "Навыки")
    private String skillProficiencies;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Владение инструментами")
    private String toolProficiency;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Снаряжение")
    private String equipment;
}
