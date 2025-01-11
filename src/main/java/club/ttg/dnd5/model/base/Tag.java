package club.ttg.dnd5.model.base;

import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.species.Species;
import club.ttg.dnd5.model.species.SpeciesFeature;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Tag name cannot be blank")
    private String name;

    @Enumerated(EnumType.STRING)
    private TagType tagType;

    @ManyToMany(mappedBy = "tags") // This is the inverse side of the relationship
    private Set<Book> books; // Set of books associated with the tag

    @ManyToMany(mappedBy = "tags")  // The "tags" field in SpeciesFeature will manage the relationship
    private Set<SpeciesFeature> speciesFeatures = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Species> species = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, TagType tagType) {
        this.name = name;
        this.tagType = tagType;
    }
}