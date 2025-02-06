package club.ttg.dnd5.model.spell.component;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellCastingTime {
    private Integer castingValue;
    private String castingType; // действие, минута, реакция
    private String custom;
    private boolean ritual;
}
