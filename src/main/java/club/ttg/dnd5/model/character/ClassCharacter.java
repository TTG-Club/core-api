package club.ttg.dnd5.model.character;

import club.ttg.dnd5.dictionary.Dice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "classes",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class ClassCharacter {
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

    @Enumerated(EnumType.STRING)
    private Dice hitDice;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Collection<ClassFeature> features;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_id")
    private ClassCharacter parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private Collection<ClassCharacter> subClasses;
}
