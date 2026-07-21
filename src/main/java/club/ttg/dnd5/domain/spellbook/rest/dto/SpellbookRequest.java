package club.ttg.dnd5.domain.spellbook.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellbookRequest {

    @Nullable
    @Size(max = 100)
    @Schema(description = "Название книги (по умолчанию — «Новая книга заклинаний»). "
            + "При обновлении null или пустая строка — не менять")
    private String name;

    @Nullable
    @Schema(description = "Слаги заклинаний, которыми сразу наполнить книгу при создании. "
            + "При обновлении не используется — заклинания добавляются отдельной ручкой")
    private Set<String> spells;
}
