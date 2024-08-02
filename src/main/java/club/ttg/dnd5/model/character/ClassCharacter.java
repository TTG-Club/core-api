package club.ttg.dnd5.model.character;

import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.model.Name;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "classes",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassCharacter extends Name {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Dice hitDice;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Collection<ClassFeature> features;
}
