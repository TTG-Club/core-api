package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Чужой лист, сохранённый по ссылке. Собирается из записи и самого листа, поэтому мапперу
 * не отдаётся: у недоступного листа половина полей берётся из записи-снимка.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SavedCharacterSheetResponse {

    @NotNull
    @Schema(description = "Идентификатор сохранённой записи (им же она и удаляется)")
    private UUID id;

    @NotNull
    @Schema(description = "Идентификатор самого листа персонажа")
    private UUID sheetId;

    @NotNull
    @Schema(description = "Токен ссылки, по которому лист открывается на чтение")
    private UUID shareToken;

    @NotNull
    @Schema(description = "Название листа: живое у доступного, снимок на момент сохранения — у остальных")
    private String name;

    @Nullable
    @Schema(description = "Лист персонажа целиком (JSON фронтового формата); null — доступ к листу закрыт")
    private JsonNode data;

    @Schema(description = "Лист всё ещё открыт по этой ссылке: не удалён и токен не отозван")
    private boolean available;
}
