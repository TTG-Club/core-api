package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class FeatureBase extends NamedEntity {
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source")
    private Book source;
}
