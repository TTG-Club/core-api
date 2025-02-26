package club.ttg.dnd5.domain.clazz.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.character.ClassFeatureLevels;
import club.ttg.dnd5.model.character.ClassSpellLevels;
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
public class ClassCharacter extends NamedEntity implements HasSourceEntity {
    @Enumerated(EnumType.STRING)
    private Ability mainAbility;

    private String genitive;

    @Enumerated(EnumType.STRING)
    private Dice hitDice;

    @Column(columnDefinition = "TEXT")
    private String equipment;
    private String armorMastery;
    private String weaponMastery;
    private String toolMastery;

    @ElementCollection(targetClass = Ability.class, fetch = FetchType.LAZY)
    @JoinTable(name = "class_saving_throw_abilities",
               joinColumns = @JoinColumn(name = "class_url"))
    @Column(name = "ability", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Ability> savingThrowMastery;

    /** Доступные умения. */
    @ElementCollection(targetClass = Skill.class, fetch = FetchType.LAZY)
    @JoinTable(name = "class_available_skills",
               joinColumns = @JoinColumn(name = "class_url"))
    @Column(name = "skill", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Skill> availableSkills;
    /** Количество умений доступных для выбора */
    private short countSkillAvailable;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "class_url")
    private Collection<ClassFeatureLevels> featureLevels;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "class_url")
    private Collection<ClassSpellLevels> classSpellLevels;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "class_url")
    private Collection<ClassFeature> features;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "parent_url")
    private ClassCharacter parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
    private Collection<ClassCharacter> subClasses;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
