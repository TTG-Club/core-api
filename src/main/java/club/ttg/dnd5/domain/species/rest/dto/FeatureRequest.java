package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureRequest {
    private NameRequest name;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    private String description;
    @Schema(description = "оригинальное описание на английском языке (обычный текст)")
    private String original;
    @Schema(description = "Уровень персонажа, с которого умение действует; null — с первого",
            example = "3")
    private Integer level;
    @Schema(description = "Механика влияния умения на лист персонажа")
    private SpeciesMechanics mechanics;
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source = new SourceResponse();
}
