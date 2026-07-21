package club.ttg.dnd5.domain.spellbook.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellbookAddSpellsRequest {

    @NotEmpty
    @Schema(description = "Слаги заклинаний из раздела заклинаний. Уже добавленные пропускаются")
    private Set<String> spells;

    @Nullable
    @Schema(description = "Пометить добавляемые заклинания подготовленными. По умолчанию false")
    private Boolean prepared;
}
