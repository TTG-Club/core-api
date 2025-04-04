package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
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
@Table(name = "items", indexes = {
        @Index(name = "url_index", columnList = "url"),
        @Index(name = "name_index", columnList = "name, english, alternative")
})
public class Item extends NamedEntity {
    @ElementCollection(targetClass = ItemType.class)
    @Enumerated(EnumType.STRING)
    private Set<ItemType> types;
    /** Стоимость предмета */
    private String cost;
    /** Вес предмета */
    private String weight;
}
