package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SpellDuration {
    private Long value;
    private DurationUnit unit;
    private String custom;
    @Override
    public String toString() {
        return Objects.nonNull(value)
                ? String.format("%s %s", value, unit.getFormattedName(value))
                : String.format( "%s", unit.getName());
    }
}
