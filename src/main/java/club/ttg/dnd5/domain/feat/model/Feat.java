package club.ttg.dnd5.domain.feat.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Черты.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feat",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Feat extends NamedEntity {
    /**
     * Категория.
     */
    @Enumerated(EnumType.STRING)
    private FeatCategory category;
    /**
     * Предварительное условие
     */
    private String prerequisite;
    /**
     * Можно брать черту больше чем один раз
     */
    private Boolean repeatability;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
