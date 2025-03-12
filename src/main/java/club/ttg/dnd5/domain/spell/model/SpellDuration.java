package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

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
    private Boolean concentration = false;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(concentration) {
            sb.append("Концентрация, ");
            if(Objects.nonNull(unit)) {
                sb.append("до ");
                sb.append(unit.getGenitiveFormattedName(value));
                sb.append(" ");
            }
        } else {
            if(Objects.nonNull(unit)) {
                sb.append(unit.getFormattedName(value));
                sb.append(" ");
            }
        }
        if(StringUtils.isNotBlank(custom)) {
            sb.append(custom);
        }
        return sb.toString().trim();
    }
}
