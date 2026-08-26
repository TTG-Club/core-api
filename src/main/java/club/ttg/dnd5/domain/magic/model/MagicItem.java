package club.ttg.dnd5.domain.magic.model;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "magic_item", indexes = {
        @Index(name = "url_index", columnList = "url"),
        @Index(name = "name_index", columnList = "name, english, alternative")
})
public class MagicItem extends NamedEntity {
    /**
     * Категория магического предмета.
     */
    @Enumerated(EnumType.STRING)
    private MagicItemCategory category;

    /**
     * Уточнение типа магического предмета, например (любой меч)
     */
    private String clarification;
    /**
     * Редкость (только для магических предметов).
     */
    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    /**
     * Текст для магических предметов с варьируемой редкостью
     */
    private String varies;

    /**
     * Настройка на магический предмет
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Attunement attunement;

    /**
     * Бонусы, которые магия даёт поверх немагического предмета: к атаке, к урону и к КД.
     * Используются листом персонажа и экспортом в VTTG. {@code null} — у записей,
     * сохранённых до появления поля.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private MagicItemBonuses bonuses;

    /**
     * Механика влияния предмета на лист персонажа: условие применения, эффекты и заряды.
     * {@code null} — у записей, сохранённых до появления поля, и у предметов, чьё действие
     * пока описано только текстом.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private MagicItemMechanics mechanics;

    /**
     * Дополнительный урон, который магия добавляет к броску немагической основы: «2к6 огнём»
     * Огненного языка, «1к6 холодом» Клинка мороза. Модель та же, что у частей урона оружия
     * и заклинания — вид части и тип урона задают токены внутри формулы.
     *
     * <p>{@code null} — предмет своего урона не добавляет.</p>
     */
    @Type(JsonType.class)
    @Column(name = "damage_parts", columnDefinition = "jsonb")
    private List<DamagePart> damageParts;

    /**
     * Количество зарядов магического предмета.
     */
    private Short charges;
    /**
     * True если предмет проклят.
     */
    private boolean curse;
    /**
     * True если расходуемый
     */
    private boolean consumable;
    /**
     * True если предметом можно пользоваться как заклинательной фокусировкой (посохи,
     * волшебные палочки, жезлы).
     */
    private boolean focus;
    /**
     * True если предмет адамантиновый.
     */
    private boolean adamantine;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;

    /**
     * Связанные немагические предметы, на основе которых создан магический предмет.
     * Используются для определения веса и стоимости при экспорте в VTTG и для фильтрации.
     * Один магический предмет может быть связан с несколькими немагическими.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @JoinTable(
            name = "magic_item_item",
            joinColumns = @JoinColumn(name = "magic_item_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Item> items;
}
