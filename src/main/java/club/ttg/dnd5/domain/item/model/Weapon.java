package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.dictionary.item.WeaponCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /** Количество дайсов или урон */
    private Short diceCount;
    /** Дайс урона */
    @Enumerated(EnumType.STRING)
    private Dice dice;
    /** Тип урона */
    @Enumerated(EnumType.STRING)
    private DamageType damageType;
    /** Свойства оружия */

    @ManyToMany
    private Collection<WeaponProperties> weaponProperties;
    /**
     * Дальнобойное оружие имеет диапазон, указанный в скобках после свойства боеприпасы или метательное.
     * Дистанция указана 2 числами. Первое — это нормальая дистанция оружия в футах.
     */
    private Short range;
    private Short rangeMax;
    /**
     * Только для универсальногол оружие. Значение урона в скобках указывается вместе с этим свойством.
     */
    private Short versatileDiceCount;
    @Enumerated(EnumType.STRING)
    private Dice versatileDice;
    /** Приёам */
    @ManyToOne(fetch = FetchType.LAZY)
    private WeaponMastery mastery;
}
