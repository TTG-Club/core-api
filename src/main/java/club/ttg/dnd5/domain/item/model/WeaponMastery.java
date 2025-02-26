package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "weapon_mastery")
public class WeaponMastery extends NamedEntity {

}