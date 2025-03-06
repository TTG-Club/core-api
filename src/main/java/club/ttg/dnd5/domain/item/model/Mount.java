package club.ttg.dnd5.domain.item.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("MOUNT")
public class Mount extends Item {
    private String carryingCapacity;
}
