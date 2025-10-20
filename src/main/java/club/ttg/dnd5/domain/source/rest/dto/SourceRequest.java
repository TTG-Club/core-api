package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceRequest extends BaseRequest {
    private String acronym;
    @Schema(description = "тип источника")
    private SourceType type;
    @Schema(description = "дата выхода книги")
    private String published;
    @Schema(description = "список авторов")
    private String authors;
    private String image;
}
