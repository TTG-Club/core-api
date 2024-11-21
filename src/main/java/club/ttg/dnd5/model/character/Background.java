package club.ttg.dnd5.model.character;

import club.ttg.dnd5.dictionary.Ability;
import club.ttg.dnd5.dictionary.Skill;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "backgrounds",
        indexes = {@Index(name = "url_index", columnList = "url")}
)
public class Background extends NamedEntity implements HasSourceEntity {
    @ElementCollection(targetClass = Ability.class, fetch = FetchType.LAZY)
    @JoinTable(name = "background_abilities",
            joinColumns = @JoinColumn(name = "background_url"))
    @Column(name = "ability", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Ability> abilities;
    /** Доступные умения. */
    @ElementCollection(targetClass = Skill.class, fetch = FetchType.LAZY)
    @JoinTable(name = "background_available_skills",
            joinColumns = @JoinColumn(name = "background_url"))
    @Column(name = "skill", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Skill> skillProficiencies;

    @ManyToOne
    private Feat feat;

    /** Владение инструментом */
    private String toolProficiency;
    /** Снаряжение */
    private String equipment;
    /** Предлагаемый класс */
    private String proposeClasses;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
