package club.ttg.dnd5.domain.beastiary.model.spellcasting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Отметки «компонент блоку НЕ требуется»: «без материальных компонентов», «без
 * компонентов заклинания» (все три сразу).
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CreatureSpellComponents {
    @Schema(description = "Вербальный компонент не требуется")
    private boolean verbal;

    @Schema(description = "Соматический компонент не требуется")
    private boolean somatic;

    @Schema(description = "Материальный компонент не требуется")
    private boolean material;
}
