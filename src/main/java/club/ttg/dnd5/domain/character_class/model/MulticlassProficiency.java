package club.ttg.dnd5.domain.character_class.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MulticlassProficiency {
    private ArmorProficiency armor;

    private WeaponProficiency weapon;

    private String toolProficiency;

    private int skills;
}
