package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SpellCastingTime {
    private Long value;
    private CastingUnit unit;
    private String custom;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(Objects.nonNull(unit)) {
            sb.append(unit.getFormattedName(value));
            sb.append(" ");
        }
        if(StringUtils.isNotBlank(custom)) {
            sb.append(custom);
        }
        return sb.toString();
    }
}
