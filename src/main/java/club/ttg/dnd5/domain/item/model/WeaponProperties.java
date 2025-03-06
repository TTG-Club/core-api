package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "weapon_poperties")
public class WeaponProperties extends NamedEntity {

}