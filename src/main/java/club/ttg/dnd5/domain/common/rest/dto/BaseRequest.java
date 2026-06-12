package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.model.TagType;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.dto.base.serializer.FormattedMarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public abstract class BaseRequest extends TagType.BaseUrl {
    @JsonProperty(value = "name")
    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameRequest name;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @JsonSerialize(using = FormattedMarkupDescriptionSerializer.class)
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @JsonProperty(value = "source")
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceRequest source;
    @Schema(description = "версия SRD, например \"5.1\"")
    private String srdVersion;
}
