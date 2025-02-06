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
public class SpellDuration {
    private Integer value;
    private String type; // минута, час, день
    private String custom;
    private boolean concentration;
}
