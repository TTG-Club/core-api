package club.ttg.dnd5.model.item;

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
     * True если предмет магический.
     */
    @Column(columnDefinition = "SMALLINT")
    private boolean magic;
    /**
     * Редкость (только для магических предметов).
     */
    @Enumerated(EnumType.STRING)
    private Rarity rarity;
    /**
     * True если требуется настройка на магический предмет или текст для выборочной настройки.
     */
    private String attunement;
    /**
     * True если предмет проклят.
     */
    private boolean curse;
    /**
     * Источник
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "source")
    private Source source = new Source();
}
