package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbilityImprovement {
    private Ability ability;
    private byte value;
}
