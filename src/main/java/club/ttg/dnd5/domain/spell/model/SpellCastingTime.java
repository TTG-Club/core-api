package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class SpellCastingTime {
    private Long value;
    private CastingUnit unit;
    private String custom;

    public static SpellCastingTime of(Long value, CastingUnit unit) {
        return SpellCastingTime.builder()
                .value(value)
                .unit(unit)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(Objects.nonNull(unit)) {
            sb.append(unit.getFormattedName(value));
        }
        if(StringUtils.isNotBlank(custom)) {
            if (!custom.startsWith(",") && !custom.startsWith(" ")) {
                sb.append(" ");
            }
            sb.append(custom);
        }
        return sb.toString();
    }
}
