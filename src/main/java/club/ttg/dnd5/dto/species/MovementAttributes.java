package club.ttg.dnd5.dto.species;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovementAttributes{
    private int base = 30;
    private int fly;
    private int climb;
    private int swim;
}
