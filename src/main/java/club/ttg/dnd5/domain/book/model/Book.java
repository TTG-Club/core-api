package club.ttg.dnd5.domain.book.model;

import club.ttg.dnd5.domain.common.model.HasTagEntity;
import club.ttg.dnd5.domain.common.model.Tag;
import club.ttg.dnd5.domain.common.model.Timestamped;
import club.ttg.dnd5.domain.common.model.Translation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "books")
@AllArgsConstructor
@Builder
public class Book extends Timestamped implements HasTagEntity {
    @Id
    @Column(unique = true, nullable = false)
    private String sourceAcronym;
    @Column(nullable = false)
    private String url;
    private String name;
    private String englishName;
    private String altName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private TypeBook type;
    private LocalDate bookDate;
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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
