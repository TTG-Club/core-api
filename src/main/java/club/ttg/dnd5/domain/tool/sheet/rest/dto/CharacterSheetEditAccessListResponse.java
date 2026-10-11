package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Права на редактирование сохранённых листов текущего пользователя.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditAccessListResponse {

    @Schema(description = "Сохранённые листы, по которым запрашивалось редактирование")
    private List<CharacterSheetEditAccessResponse> sheets;
}
