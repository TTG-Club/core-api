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
    private Long value;
    private DistanceUnit unit;
    private String custom;
    @Override
    public String toString() {
        return Objects.nonNull(value)
                ? String.format("%s %s", value, unit.getFormattedName(value))
                : String.format( "%s", unit.getName());
    }
}
