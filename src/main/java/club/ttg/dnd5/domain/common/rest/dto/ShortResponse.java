package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.dto.base.SourceResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ShortResponse {
    private String url;
    private NameResponse name = new NameResponse();
    @JsonProperty(value = "source")
    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceResponse source = new SourceResponse();
    @Schema(description = "дата обновления")
    private String updatedAt;
}