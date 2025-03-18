package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.model.TagType;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public abstract class BaseRequest extends TagType.BaseUrl {
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameRequest name;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @Schema(description = "описание c markup")
    private String markupDescription;
    @JsonProperty(value = "source")
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceRequest source;
    @Schema(description = "дата обновления")
    private Instant updatedAt;
    private String userId;
}
