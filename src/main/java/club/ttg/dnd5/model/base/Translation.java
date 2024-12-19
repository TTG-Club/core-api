package club.ttg.dnd5.model.base;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Translation {
    @ElementCollection
    @CollectionTable(
            name = "translation_authors", // Name of the table for translation authors
            joinColumns = @JoinColumn(name = "translation_id") // Foreign key linking to the translation
    )
    @Column(name = "author_name") // Column for the author names
    private Set<String> authors = new HashSet<>();

    @Column(name = "translation_date")
    private LocalDate translationDate;
}
