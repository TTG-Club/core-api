package club.ttg.dnd5.domain.beastiary.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
public class CreatureEquipment {
    private String url;
    private String name;
    private Byte quantity;
}
