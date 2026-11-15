package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.source.model.SourceCategory;
import club.ttg.dnd5.domain.source.model.SourceOfficiality;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceRequest extends BaseRequest {
    private String acronym;
    @Schema(description = "Степень официальности источника")
    private SourceOfficiality officiality;
    @Schema(description = "Категория источника")
    private SourceCategory category;
    @Schema(description = "Издатель")
    private PublisherDto publisher;
    @Schema(description = "Перевод")
    private TranslationDto translation;
}
