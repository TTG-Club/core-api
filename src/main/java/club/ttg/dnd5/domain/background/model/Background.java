package club.ttg.dnd5.domain.background.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.HasSourceEntity;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.book.model.Source;
import club.ttg.dnd5.domain.feat.model.Feat;
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

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "feat_id")
    private Feat feat;

    /** Владение инструментами */
    private String toolProficiency;
    /** Снаряжение */
    private String equipment;
    /** Предлагаемый класс */
    private String proposeClasses;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
