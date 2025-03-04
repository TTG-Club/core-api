package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "species_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class SpeciesFeature extends NamedEntity {
    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
}
