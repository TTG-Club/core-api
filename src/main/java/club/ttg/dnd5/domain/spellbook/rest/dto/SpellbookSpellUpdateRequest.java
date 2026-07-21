package club.ttg.dnd5.domain.spellbook.rest.dto;

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
public class SpellbookSpellUpdateRequest {

    @NotNull
    @Schema(description = "Заклинание подготовлено")
    private Boolean prepared;
}
