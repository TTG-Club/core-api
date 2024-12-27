package club.ttg.dnd5.dto.species;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovementAttributes{
    private final int base;
    private int fly;
    private int climb;
    private int swim;

    public MovementAttributes() {
        this.base = 30;
        fly = -1;
        climb = -1;
        swim = -1;
    }
}
