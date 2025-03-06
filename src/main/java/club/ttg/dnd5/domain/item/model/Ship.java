package club.ttg.dnd5.domain.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("SHIP")
public class Ship extends Item {
    private String speed;
    private String crew;
    private String passengers;
    private String cargo;
    @JsonProperty(value = "ac")
    private String armorClass;
    @JsonProperty(value = "hp")
    private String hitPoints;
    private String damageThreshold;

}
