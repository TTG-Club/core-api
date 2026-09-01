package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.species.model.Species;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

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
    private Source source;
    private Long sourcePage;

    /**
     * Виды, которым заклинание принадлежит.
     *
     * <p>Таблица связи общая с врождёнными заклинаниями вида: там же лежит колонка
     * {@code required_level}, которой эта связь не знает (см.
     * {@code SpeciesRepository#findInnateSpells}). Поэтому коллекцию нельзя подменять
     * новой — Hibernate счёл бы её выброшенной, удалил все строки вместе с уровнями и
     * создал заново с единицей. Правится она на месте, в
     * {@code SpellService#syncAffiliation}: тогда в базу уходит только разница.</p>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "spell_species_affiliation",
            joinColumns = @JoinColumn(name = "spell_url"),
            inverseJoinColumns = @JoinColumn(name = "species_affiliation_url")
    )
    private Set<Species> speciesAffiliation;

    /** Происхождения видов; таблица связи общая с их врождёнными заклинаниями — см. {@link #speciesAffiliation}. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "spell_lineages_affiliation",
            joinColumns = @JoinColumn(name = "spell_url"),
            inverseJoinColumns = @JoinColumn(name = "lineages_affiliation_url")
    )
    private Set<Species> lineagesAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<CharacterClass> classAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<CharacterClass> subclassAffiliation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "spell_feat_affiliation",
            joinColumns = @JoinColumn(name = "spell_id"),
            inverseJoinColumns = @JoinColumn(name = "feat_id")
    )
    private Set<Feat> featAffiliation;

    private Boolean upcastable;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private SpellEffect effect;

    @Type(JsonType.class)
    @Column(name = "active_effects", columnDefinition = "jsonb")
    private List<ActiveEffect> activeEffects;
}
