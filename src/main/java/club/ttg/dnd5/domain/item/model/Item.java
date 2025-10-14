package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

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

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;

}
