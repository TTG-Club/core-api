package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Итог запроса на редактирование: новый запрос ждёт ответа, а уже выданное право так и остаётся.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditRequestResponse {

    @NotNull
    @Schema(description = "PENDING — запрос ждёт ответа владельца, APPROVED — право уже выдано")
    private CharacterSheetEditorStatus status;
}
