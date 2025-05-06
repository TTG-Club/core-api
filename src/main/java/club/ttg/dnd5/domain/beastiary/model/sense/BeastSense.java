package club.ttg.dnd5.domain.beastiary.model.sense;

import club.ttg.dnd5.domain.beastiary.model.SenseType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastSense {
    private SenseType type;
    private int value;
}
