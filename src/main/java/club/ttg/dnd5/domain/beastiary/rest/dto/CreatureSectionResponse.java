package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureSectionResponse {
    @Schema(description = "Название секции")
    private NameRequest name;

    @Schema(description = "Подзаголовок секции")
    private String subtitle;
    @Schema(description = "Места обитания")
    private String habitats;

    @Schema(description = "Сокровища")
    private String treasures;

    @Schema(description = "Описание секции")
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;
}
