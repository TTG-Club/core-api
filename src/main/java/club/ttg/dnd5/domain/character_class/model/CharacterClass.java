package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "class",
        indexes = {
                @Index(name = "class_url_index", columnList = "url"),
                @Index(name = "class_name_index", columnList = "name, english, alternative")
        }
)
public class CharacterClass extends NamedEntity {

    @ManyToOne
    @JoinColumn(name = "parent_url")
    private CharacterClass parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<CharacterClass> subclasses;

    private Dice hitDice;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> primaryCharacteristics;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private ArmorProficiency armorProficiency;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private WeaponProficiency weaponProficiency;

    private String toolProficiency;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private SkillProficiency skillProficiency;

    @Column(columnDefinition = "TEXT")
    private String equipment;

    @Enumerated(EnumType.STRING)
    private CasterType casterType;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> savingThrows;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<ClassFeature> features;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "class_table")
    private List<ClassTableColumn> table;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
