package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.Roll;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Damage {
    private Roll roll;
    private DamageType type;

    public String toString() {
        return "%s %s".formatted(roll.toString(), type.name());
    }
}
