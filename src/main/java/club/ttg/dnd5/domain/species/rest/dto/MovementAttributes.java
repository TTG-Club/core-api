package club.ttg.dnd5.domain.species.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
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
