package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SourceDetailResponse extends BaseResponse {

    private String type;
    private String image;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String authors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslationDto translation;
    @Schema(description = "Издатель")
    private PublisherDto publisher;
}
