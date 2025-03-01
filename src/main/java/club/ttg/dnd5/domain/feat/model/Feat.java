package club.ttg.dnd5.domain.feat.model;

import club.ttg.dnd5.domain.common.model.HasSourceEntity;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.book.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Черты.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feats",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Feat extends NamedEntity implements HasSourceEntity {
    @Enumerated(EnumType.STRING)
    private FeatCategory category;
    private String prerequisite;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
