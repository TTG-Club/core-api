package club.ttg.dnd5.domain.beastiary.model.section;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.CreatureTreasure;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "beast_section")
public class CreatureSection extends NamedEntity {
    /**
     * Подзаголовок секции
     */
    private String subtitle;
    /**
     * Места обитания
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<Habitat> habitats;

    /**
     * Сокровища
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureTreasure> treasures;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "section")
    private Collection<Creature> creatures;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
