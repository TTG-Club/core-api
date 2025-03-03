package club.ttg.dnd5.domain.book.model;

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
public class Book extends Timestamped {
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

    public Book(String source) {
        this.sourceAcronym = source;
    }
}
