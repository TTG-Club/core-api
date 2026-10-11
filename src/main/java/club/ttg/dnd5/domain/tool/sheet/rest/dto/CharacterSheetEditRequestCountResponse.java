package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Число неотвеченных запросов на редактирование листов пользователя — для точки у шлема.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditRequestCountResponse {

    @Schema(description = "Неотвеченные запросы на активные листы текущего пользователя")
    private long count;
}
