package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureSection {
    private String sectionName;
    private String sectionEnglish;
    /**
     * Подзаголовок секции
     */
    private String subtitle;
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String sectionDescription;

    /**
     * Места обитания
     */
    private Collection<Habitat> habitats;

    /**
     * Сокровища
     */
    private Collection<CreatureTreasure> treasures;
}
