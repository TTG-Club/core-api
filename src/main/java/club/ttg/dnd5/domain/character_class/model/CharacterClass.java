package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
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
    /**
     * Родительский класс для подкласса
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_url")
    private CharacterClass parent;

    /**
     * Подклассы
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<CharacterClass> subclasses;

    /**
     * Кость хитов
     */
    private Dice hitDice;

    /**
     * Основные характеристики
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> primaryCharacteristics;

    /**
     * Разделитель для основных характеристик
     */
    @Column(name = "delimiter_primary")
    @Enumerated(EnumType.STRING)
    private Delimiter delimiterPrimary;

    /**
     * Умение ношение доспехов
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private ArmorProficiency armorProficiency;

    /**
     * Умение обращения с оружием
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private WeaponProficiency weaponProficiency;

    /**
     * Умение обращения с инструментами
     */
    private String toolProficiency;

    /**
     * Умение в навыках
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private SkillProficiency skillProficiency;

    @Type(JsonType.class)
    @Column(name = "multiclass", columnDefinition = "jsonb")
    private MulticlassProficiency multiclassProficiency;

    /**
     * Снаряжение
     */
    @Column(columnDefinition = "TEXT")
    private String equipment;

    /**
     * Тип заклинателя
     */
    @Enumerated(EnumType.STRING)
    private CasterType casterType;

    /**
     * Спасброски
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> savingThrows;

    /**
     * Умения класса
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<ClassFeature> features;

    /**
     * Таблица класса
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "class_table")
    private List<ClassTableColumn> table;

    /**
     * Шаблон характеристик для стандартного набора
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "ability_template")
    private List<Integer> abilityTemplate;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
