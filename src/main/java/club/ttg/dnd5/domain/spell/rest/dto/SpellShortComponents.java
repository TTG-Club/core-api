package club.ttg.dnd5.domain.spell.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SpellShortComponents {
    private Boolean v;
    private Boolean s;
    private Boolean m;
    /**
     * У материального компонента указана стоимость — такой компонент нельзя заменить
     * фокусировкой или мешочком с компонентами.
     */
    @Schema(description = "Материальный компонент со стоимостью")
    private Boolean withCost;
    /** Материальный компонент расходуется при накладывании заклинания. */
    @Schema(description = "Материальный компонент расходуется")
    private Boolean consumable;
}
