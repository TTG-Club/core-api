package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
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
    @Type(
            value = ListArrayType.class,
            parameters = {
                    @org.hibernate.annotations.Parameter(
                            name = ListArrayType.SQL_ARRAY_TYPE,
                            value = "item_type"
                    )
            }
    )
    @Column(
            name = "item_types",
            columnDefinition = "item_type[]"
    )
    private Set<ItemType> types;
    /** Стоимость предмета */
    private String cost;
    /** Вес предмета */
    private String weight;
}
