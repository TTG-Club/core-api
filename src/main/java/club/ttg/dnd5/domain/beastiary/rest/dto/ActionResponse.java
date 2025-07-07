package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionResponse {
    @Schema(description = "Название")
    private Name name;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    @Schema(description = "Описание")
    private String description;
}
