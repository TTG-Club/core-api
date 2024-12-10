package club.ttg.dnd5.model.book;

import club.ttg.dnd5.model.base.HasTagEntity;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TimestampedEntity;
import club.ttg.dnd5.model.base.Translation;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "books")
@AllArgsConstructor
@Builder
public class Book extends TimestampedEntity implements HasTagEntity {
    @Id
    @Column(unique = true, nullable = false)
    private String sourceAcronym;
    private String name;
    private String altName;
    private String englishName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private TypeBook type;
    private Integer year;
    private String image;

    @Embedded
    private Translation translation;

    // Collection of authors for the book itself
    @ElementCollection
    @CollectionTable(
            name = "book_authors", // Name of the table for book authors
            joinColumns = @JoinColumn(name = "book_id") // Foreign key linking to the book
    )
    @Column(name = "author_name") // Column for the author names
    private Set<String> authors = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "book_tags", // name of the join table
            joinColumns = @JoinColumn(name = "book_id"), // foreign key for the book
            inverseJoinColumns = @JoinColumn(name = "tag_id") // foreign key for the tag
    )
    private Set<Tag> tags = new HashSet<>(); // Set of tags associated with the book

    public Book(String source) {
        this.sourceAcronym = source;
    }
}
