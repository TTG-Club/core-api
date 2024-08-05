package club.ttg.dnd5.model.character;

import club.ttg.dnd5.model.Source;
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
public class Specie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_id")
    private Specie parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private Collection<Specie> subSpecies;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Short page;
}
