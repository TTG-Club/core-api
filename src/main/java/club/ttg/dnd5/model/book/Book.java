package club.ttg.dnd5.model.book;

import club.ttg.dnd5.model.base.TimestampedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "books")
public class Book extends TimestampedEntity {
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

    public Book(String source) {
        this.sourceAcronym = source;
    }
}
