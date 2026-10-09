package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Право на редактирование сохранённого чужого листа — лёгкая сводка без документа. По ней клиент
 * сразу узнаёт, что владелец разрешил правки, и сообщает об этом без перезагрузки страницы.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetEditAccessResponse {

    @NotNull
    @Schema(description = "Идентификатор сохранённой записи")
    private UUID savedId;

    @NotNull
    @Schema(description = "Идентификатор самого листа персонажа")
    private UUID sheetId;

    @NotNull
    @Schema(description = "Название листа из сохранённой записи")
    private String name;

    @NotNull
    @Schema(description = "PENDING — ждёт ответа владельца, APPROVED — можно редактировать, "
            + "DECLINED — отказ из прежних версий; можно сразу попросить снова")
    private CharacterSheetEditorStatus status;
}
