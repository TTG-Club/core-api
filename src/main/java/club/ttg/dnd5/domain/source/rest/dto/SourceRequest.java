package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.source.model.SourceKind;
import club.ttg.dnd5.domain.source.model.SourceOrigin;
import club.ttg.dnd5.domain.source.model.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SourceRequest extends BaseRequest {
    private String acronym;
    @Schema(description = "тип источника")
    @Deprecated(forRemoval = true)
    private SourceType type;
    @Schema(description = "РџСЂРѕРёСЃС…РѕР¶РґРµРЅРёРµ РёСЃС‚РѕС‡РЅРёРєР°")
    private SourceOrigin origin;
    @Schema(description = "Р¤РѕСЂРјР°С‚ РёСЃС‚РѕС‡РЅРёРєР°")
    private SourceKind kind;
    @Schema(description = "Издатель")
    private PublisherDto publisher;
    @Schema(description = "Перевод")
    private TranslationDto translation;
}
