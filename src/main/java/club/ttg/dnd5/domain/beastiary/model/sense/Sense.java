package club.ttg.dnd5.domain.beastiary.model.sense;

import club.ttg.dnd5.domain.common.dictionary.SenseType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sense {
    private SenseType type;
    private int value;
}
