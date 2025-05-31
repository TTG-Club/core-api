package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureSection {
    @Schema(description = "Подзаголовок секции")
    private String subtitle;
    @Schema(description = "Места обитания")
    private Collection<Habitat> habitats;

    @Schema(description = "Сокровища")
    private Collection<CreatureTreasure> treasures;
}
