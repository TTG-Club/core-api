package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.Source;
import club.ttg.dnd5.model.base.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 Виды или разновидности (расы)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "species",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class Species extends NamedEntity {
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_id")
    private Species parent;
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private Collection<Species> subSpecies;
    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
}
