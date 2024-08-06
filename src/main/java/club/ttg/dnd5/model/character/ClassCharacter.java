package club.ttg.dnd5.model.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dictionary.Skill;
import club.ttg.dnd5.model.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "classes",
        indexes = {
            @Index(name = "url_index", columnList = "url")
        }
)
public class ClassCharacter {
    @Id
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
    private String genitive;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Dice hitDice;

    @Column(columnDefinition = "TEXT")
    private String equipment;
    private String armorMastery;
    private String weaponMastery;
    private String toolMastery;

    @ElementCollection(targetClass = Ability.class)
    @JoinTable(name = "class_saving_throw_abilities", joinColumns = @JoinColumn(name = "class_url"))
    @Column(name = "ability", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Ability> savingThrowMastery;

    @ElementCollection(targetClass = Skill.class)
    @JoinTable(name = "class_available_skills", joinColumns = @JoinColumn(name = "class_url"))
    @Column(name = "skill", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Skill> availableSkills;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Collection<ClassFeature> features;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_id")
    private ClassCharacter parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private Collection<ClassCharacter> subClasses;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Short page;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime created;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdated;
}
