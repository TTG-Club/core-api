package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class SpellDistance {
    private Long distance;
    private DistanceUnit distanceUnit;

    @Override
    public String toString() {
        return Objects.nonNull(distance)
                ? String.format("%s %s", distance, distanceUnit.getFormattedName(distance))
                : String.format( "%s", distanceUnit.getName());
    }
}
