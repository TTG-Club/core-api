package club.ttg.dnd5.model.spell.component;

import club.ttg.dnd5.model.spell.enums.TimeUnit;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellcastingTime {
    private Integer castingValue;
    @Enumerated(EnumType.STRING)
    private TimeUnit castingType; // действие, минута, реакция
    private String custom;
    private boolean ritual;
}
