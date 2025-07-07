package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
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
    private WeaponCategory weaponCategory;

    /**
     * Урон
     */
    @Type(JsonType.class)
    @Column(name = "weapon_damage", columnDefinition = "jsonb")
    private Damage damage;

    /**
     * Свойства оружия
     */
    @Type(JsonType.class)
    @Column(name = "weapon_properties", columnDefinition = "jsonb")
    private Collection<Property> properties;

    /** Приёам */
    @Type(JsonType.class)
    @Column(name = "weapon_mastery", columnDefinition = "jsonb")
    private Mastery mastery;
}
