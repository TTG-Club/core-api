package club.ttg.dnd5.domain.clazz.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.model.HasTagEntity;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.common.model.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_features",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassFeature extends NamedEntity implements HasTagEntity {
    private short level;
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
            name = "class_feature_tags", // Name of the join table
            joinColumns = @JoinColumn(name = "class_feature_id"), // Foreign key for ClassFeature
            inverseJoinColumns = @JoinColumn(name = "tag_id") // Foreign key for Tag
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
}
