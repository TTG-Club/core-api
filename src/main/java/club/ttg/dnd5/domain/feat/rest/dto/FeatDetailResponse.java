package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FeatDetailResponse extends BaseResponse {
    @Schema(description = "Категория", examples = {"черта происхождения", "общая черта"})
    private String category;
    @Schema(description = "Предварительное условие", examples = {"черта происхождения", "общая черта"})
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String prerequisite;
}
