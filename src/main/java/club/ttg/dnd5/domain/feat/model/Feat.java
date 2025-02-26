package club.ttg.dnd5.domain.feat.model;

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
@Table(name = "feats",
        indexes = {@Index(name = "idx_url", columnList = "url")}
)
public class Feat extends NamedEntity implements HasSourceEntity {
    @Enumerated(EnumType.STRING)
    private FeatCategory category;
    private String prerequisite;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
