package club.ttg.dnd5.domain.spellbook.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Книги пользователя двумя списками: сначала свои, ниже — доступные по ссылке.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpellbookListResponse {

    @NotNull
    @Schema(description = "Свои книги, новые первее. Только они считаются в лимите без подписки")
    private List<SpellbookShortResponse> own;

    @NotNull
    @Schema(description = "Чужие книги, добавленные по ссылке (только чтение); "
            + "последние добавленные первее")
    private List<SpellbookShortResponse> shared;
}
