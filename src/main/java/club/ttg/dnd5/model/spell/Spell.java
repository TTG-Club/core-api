package club.ttg.dnd5.model.spell;

import club.ttg.dnd5.model.base.HasTagEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.book.Source;
import club.ttg.dnd5.model.spell.component.*;
import club.ttg.dnd5.model.spell.enums.SpellDistance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "spells")
public class Spell extends NamedEntity implements HasTagEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source; // Источник заклинания

    @Column(nullable = false)
    private int level; // Уровень заклинания (0 - заговор)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private MagicSchool school; // Школа магии

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "custom", column = @Column(name = "distance_custom"))
    })
    private SpellDistance distance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "custom", column = @Column(name = "duration_custom"))
    })
    private SpellDuration duration;

    @Embedded
    private SpellCastingTime castingTime; // Время накладывания

    @Embedded
    private SpellComponents components; // Компоненты

    @ManyToMany
    @JoinTable(
            name = "spell_tags",
            joinColumns = @JoinColumn(name = "spell_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Embedded
    private SpellAffiliation affiliation; // Связь с классами, расами и т.д.

    @Column(columnDefinition = "TEXT")
    private String upper; // "На более высоких уровнях"
}
