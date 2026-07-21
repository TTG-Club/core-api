package club.ttg.dnd5.domain.spellbook.rest.dto;

import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellbookSpellResponse {

    @NotNull
    @Schema(description = "Заклинание в том же виде, что и в разделе заклинаний")
    private SpellShortResponse spell;

    @Schema(description = "Заклинание подготовлено")
    private boolean prepared;
}
