package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@MappedSuperclass
public abstract class FeatureBase extends NamedEntity {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source;
    private String featureDescription;

    @ManyToMany
    @JoinTable(
            name = "book_tags",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
