package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.dictionary.Delimiter;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
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
    @Column(name = "parent_url")
    private String parentUrl;

    /**
     * Родительский класс для подкласса
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_url", insertable = false, updatable = false)
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
     * Стартовое снаряжение вариантами выбора: «А» — предметы, «Б» — монеты и т.д.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "starting_equipment")
    private List<EquipmentOption> startingEquipment;

    /**
     * Тип заклинателя
     */
    @Enumerated(EnumType.STRING)
    private CasterType casterType;

    /**
     * Характеристика, которой класс колдует.
     *
     * <p>До её появления потребители выводили характеристику по каноническому ключу
     * класса, и у самописного или переведённого класса она молча пропадала. Пусто —
     * класс не заклинатель либо характеристика в источнике не указана.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "spellcasting_ability")
    private Ability spellcastingAbility;

    /**
     * Уровень класса, с которого работает заклинательство. Пусто — с первого
     * (у трети-заклинателей это третий).
     */
    @Column(name = "spellcasting_start_level")
    private Integer spellcastingStartLevel;

    /**
     * Подпись группы подклассов: «Воинский архетип», «Магическая традиция». Пусто —
     * потребитель показывает нейтральное «Подкласс».
     */
    @Column(name = "subclass_label")
    private String subclassLabel;

    /**
     * Уровень, на котором выбирается подкласс. Пусто — уровень берётся из уровня
     * первого умения подкласса.
     */
    @Column(name = "subclass_level")
    private Integer subclassLevel;

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

    /**
     * Механика влияния самого класса на лист персонажа: то, что даёт взятие класса
     * целиком, а не отдельное его умение.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private ClassMechanics mechanics;

    /**
     * Активные эффекты класса в вокабуляре VTTG — та же модель и та же колонка, что у
     * черты ({@code Feat.activeEffects}) и предмета.
     *
     * <p>Своей колонкой, а не полем внутри {@link #mechanics}: механика описывает дары,
     * которые лист проставляет сам, а эффект меняет числа готовой формулой и уезжает на
     * виртуальный стол как есть.</p>
     */
    @Type(JsonType.class)
    @Column(name = "active_effects", columnDefinition = "jsonb")
    private List<ActiveEffect> activeEffects;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
