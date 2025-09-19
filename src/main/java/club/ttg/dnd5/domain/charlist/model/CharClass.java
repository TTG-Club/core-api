package club.ttg.dnd5.domain.charlist.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharClass {
    private String name;
    private byte level;
    private boolean base;
    private HitDice hitDice;
}
