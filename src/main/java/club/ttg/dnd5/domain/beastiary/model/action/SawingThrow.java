package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SawingThrow {
    private Ability ability;

    private byte dc;
}
