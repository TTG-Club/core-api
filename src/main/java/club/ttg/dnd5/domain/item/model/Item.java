package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.dictionary.item.ItemType;
import club.ttg.dnd5.dictionary.item.magic.Rarity;
import club.ttg.dnd5.model.base.HasSourceEntity;
import club.ttg.dnd5.model.base.NamedEntity;
import club.ttg.dnd5.model.book.Source;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Entity
@Table(name = "items")
public class Item extends NamedEntity implements HasSourceEntity {
    @Enumerated(EnumType.STRING)
    private Set<ItemType> types;
    /** Стоимость предмета */
    private String cost;
    /** Вес предмета */
    private String weight;

    /**
     * Источник
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
