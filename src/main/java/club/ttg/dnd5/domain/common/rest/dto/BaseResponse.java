package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.model.TagType;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public abstract class BaseResponse extends TagType.BaseUrl {
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameResponse name;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
    @JsonProperty(value = "source")
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source = new SourceResponse();
    @Schema(description = "дата обновления")
    private Instant updatedAt;
    private String userId;
    @Schema(description = "пользователь который последний обновил")
    private String lastUsername;
}
