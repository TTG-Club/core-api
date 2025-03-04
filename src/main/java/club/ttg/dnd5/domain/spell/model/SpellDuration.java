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
    private Long duration;
    private DurationUnit durationUnit;

    @Override
    public String toString() {
        return Objects.nonNull(duration)
                ? String.format("%s %s", duration, durationUnit.getFormattedName(duration))
                : String.format( "%s", durationUnit.getName());
    }
}
