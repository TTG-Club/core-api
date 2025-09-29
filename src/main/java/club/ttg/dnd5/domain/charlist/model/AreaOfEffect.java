package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.AreaOfEffectType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AreaOfEffect {
    private AreaOfEffectType type;
    private short size;
}
