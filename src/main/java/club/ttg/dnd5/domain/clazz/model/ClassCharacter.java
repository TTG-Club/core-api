package club.ttg.dnd5.domain.clazz.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "classes",
        indexes = {
            @Index(name = "url_index", columnList = "url"),
            @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class ClassCharacter extends NamedEntity {

    @Enumerated(EnumType.STRING)
    private Ability mainAbility;

    @Enumerated(EnumType.STRING)
    private Dice hitDice;

    @Column(columnDefinition = "TEXT")
    private String equipment;
    private String armorMastery;
    private String weaponMastery;
    private String toolMastery;

    @ElementCollection(targetClass = Ability.class)
    @JoinTable(name = "class_saving_throw_abilities",
               joinColumns = @JoinColumn(name = "class_url")
    )
    @Column(name = "ability")
    @Enumerated(EnumType.STRING)
    private Set<Ability> savingThrowMastery;

    @ElementCollection(targetClass = Skill.class, fetch = FetchType.LAZY)
    @JoinTable(name = "class_available_skills",
               joinColumns = @JoinColumn(name = "class_url"))
    @Column(name = "skill", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Skill> availableSkills;

    private short countSkillAvailable;

    @ManyToMany
    @JoinTable(
            name = "class_feature_relationships",
            joinColumns = @JoinColumn(name = "class_url", referencedColumnName = "url"),
            inverseJoinColumns = @JoinColumn(name = "feature_url", referencedColumnName = "url")
    )
    private Collection<ClassFeature> features;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_url")
    private ClassCharacter parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
    private Collection<ClassCharacter> subClasses;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;

    private Short sourcePage;
}
