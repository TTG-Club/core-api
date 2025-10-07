package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.NamedEntity;
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
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

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
