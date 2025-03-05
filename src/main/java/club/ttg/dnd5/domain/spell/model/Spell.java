package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.species.model.Species;
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

    @Column(nullable = false)
    private Boolean ritual;
    @Column(nullable = false)
    private Boolean concentration;
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
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

    private String upper;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Species> speciesAffiliation;
    //TODO раскомментить после рождения классов
//    @ManyToMany(fetch = FetchType.LAZY)
//    private List<ClassCharacter> classAffiliation;
}
