package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@Entity
public class CreatureSection {
    @Id
    @Column(name = "url")
    private String url;

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
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "creature_section_habitats", joinColumns = @JoinColumn(name = "creature_section_url"))
    @Column(name = "habitat")
    private Set<Habitat> habitats;

    /**
     * Сокровища
     */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "creature_section_treasures", joinColumns = @JoinColumn(name = "creature_treasures_url"))
    @Column(name = "treasures")
    private Collection<CreatureTreasure> treasures;
}
