package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.dictionary.item.WeaponCategory;
import club.ttg.dnd5.domain.item.model.Item;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Collection;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("WEAPON")
public class Weapon extends Item {
    /** Категория оружия */
    @Enumerated(EnumType.STRING)
    private WeaponCategory category;

    /**
     * Урон
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Damage damage;

    /**
     * Свойства оружия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<Property> properties;

    /** Приёам */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Mastery mastery;
}
