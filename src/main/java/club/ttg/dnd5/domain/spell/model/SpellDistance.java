package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

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

    public static SpellDistance of(Long value, DistanceUnit unit) {
        return SpellDistance.builder()
                .value(value)
                .unit(unit)
                .build();
    }

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
