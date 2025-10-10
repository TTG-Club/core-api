package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Weapon  {
    /** Категория оружия */
    private WeaponCategory category;

    /**
     * Тип урона
     */
    private Damage damage;

    /**
     * Свойства оружия
     */
    private Set<Property> properties;

    /** Приём */
    private Mastery mastery;

    private Range range;
    private Roll versatile;

    /**
     * Требуемый тип снаряда для выстрела (только дальнобойного)
     */
    private AmmunitionType ammo;

    private String additional;
}
