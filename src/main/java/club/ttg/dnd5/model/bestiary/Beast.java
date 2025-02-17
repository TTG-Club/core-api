package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Alignment;
import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.BeastType;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "bestiary")
public class Beast extends NamedEntity implements HasSourceEntity {
    /**
     * Размер существа.
     */
    @Enumerated(EnumType.STRING)
    private Size size;

    @Enumerated(EnumType.STRING)
    private BeastType type;

    private String tags;
    @Enumerated(EnumType.STRING)
    private Alignment alignment;

    private byte AC;
    private String descriptionAC;

    private short hit;
    private String descriptionHit;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
