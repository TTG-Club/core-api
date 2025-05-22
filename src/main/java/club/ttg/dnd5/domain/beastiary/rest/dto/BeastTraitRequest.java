package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastTraitRequest {
    @Schema(description = "Название особенности")
    private Name name;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    @Schema(description = "Описание особенности")
    private String description;
}
