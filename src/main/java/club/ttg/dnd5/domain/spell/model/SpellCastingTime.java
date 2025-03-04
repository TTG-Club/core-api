package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SpellCastingTime {
    private Long castingTime;
    private CastingUnit castingUnit;

    @Override
    public String toString() {
        return Objects.nonNull(castingTime)
                ? String.format("%s %s", castingTime, castingUnit.getFormattedName(castingTime))
                : String.format( "%s", castingUnit.getName());
    }
}
