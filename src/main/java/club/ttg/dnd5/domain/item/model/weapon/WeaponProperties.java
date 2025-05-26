package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "weapon_property")
public class WeaponProperties extends NamedEntity {
    private boolean hasDistance;
    private boolean hasVersatile;
    private boolean hasAmmo;
}