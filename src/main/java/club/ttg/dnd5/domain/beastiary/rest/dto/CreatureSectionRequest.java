package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureSectionRequest {
    @Schema(description = "Название секции")
    private NameRequest name;

    @Schema(description = "Подзаголовок секции")
    private String subtitle;
    @Schema(description = "Места обитания")
    private Collection<Habitat> habitats;

    @Schema(description = "Сокровища")
    private Collection<CreatureTreasure> treasures;

    @Schema(description = "Описание секции")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;
}
