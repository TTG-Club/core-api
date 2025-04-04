package club.ttg.dnd5.domain.clazz.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;


@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassFeature extends NamedEntity {

    private String quote;
    private short level;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;

    private Short sourcePage;

    @ManyToMany(mappedBy = "features")
    private Collection<ClassCharacter> classCharacters;
}
