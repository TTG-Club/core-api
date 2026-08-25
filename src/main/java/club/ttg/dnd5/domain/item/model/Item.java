package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
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
@Table(name = "item", indexes = {
        @Index(name = "url_index", columnList = "url"),
        @Index(name = "name_index", columnList = "name, english, alternative")
})
public class Item extends NamedEntity {
    @Enumerated(EnumType.STRING)
    private ItemCategory category = ItemCategory.ITEM;

    @Type(JsonType.class)
    @Column(name = "item_types", columnDefinition = "jsonb")
    private Set<ItemType> types;
    /** Стоимость предмета */
    private String cost;
    /** Номинал монеты */
    @Enumerated(EnumType.STRING)
    private Coin coin;
    /** Вес предмета */
    private String weight;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Armor armor;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Weapon weapon;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Tool tool;

    /**
     * Категория снаряжения в вокабуляре VTTG ({@code EquipmentCategory}):
     * {@code adventurer-equipment}, {@code trinket}, {@code clothing}, {@code ring},
     * {@code wand}, {@code wondrous}, {@code food}, {@code vehicle-equipment}.
     *
     * <p>Броню задаёт {@link #armor} своим словарём сайта, поэтому её значения здесь не
     * ожидаются. Пусто — категория выводится из типов предмета, как раньше.</p>
     */
    @Column(name = "equipment_category")
    private String equipmentCategory;

    /**
     * Активные эффекты предмета в вокабуляре VTTG — та же модель, что у заклинания,
     * черты и магического предмета.
     *
     * <p>Своей колонкой, а не полем внутри {@link #weapon}/{@link #armor}: эффект живёт у
     * предмета любого рода, а подформа — только у своего. Так же лежит и в самой системе
     * dnd5e-2024, где {@code activeEffects} — поле предмета, а не его боевого блока.</p>
     */
    @Type(JsonType.class)
    @Column(name = "active_effects", columnDefinition = "jsonb")
    private List<ActiveEffect> activeEffects;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
