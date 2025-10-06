package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "spell",
        indexes = {
                @Index(name = "spell_url_index", columnList = "url"),
                @Index(name = "spell_name_index", columnList = "name, english, alternative")
        }
)
public class Spell extends NamedEntity {

    @Column(nullable = false)
    private Long level;
    @Embedded
    private SpellSchool school;

    @Embedded
    private SpellComponents components;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<SpellDistance> range;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<SpellCastingTime> castingTime;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<SpellDuration> duration;

    @Column(columnDefinition = "TEXT")
    private String upper;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Species> speciesAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Species> lineagesAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<CharacterClass> classAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<CharacterClass> subclassAffiliation;

    private Boolean upcastable;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Ability> savingThrow;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<HealingType> healingType;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<DamageType> damageType;
}
