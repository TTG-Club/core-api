package club.ttg.dnd5.model.book;

import club.ttg.dnd5.model.base.HasTagEntity;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TimestampedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "books")
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

    @ManyToMany
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
