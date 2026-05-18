package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.AreaOfEffectType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaOfEffect {
    private AreaOfEffectType type;
    private int value1;
    private Integer value2;
}
