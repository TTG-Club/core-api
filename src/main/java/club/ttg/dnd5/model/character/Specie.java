package club.ttg.dnd5.model.character;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

/**
  Виды или разновидности (расы)
 */
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
}
