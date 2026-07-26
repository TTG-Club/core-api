package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Лист, открытый по ссылке. Отдельный DTO, а не {@link CharacterSheetResponse}: наружу уходит
 * только то, что нужно для отображения — без токена ссылки, флага удаления и служебных дат.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetPublicResponse {

    @NotNull
    @Schema(description = "Идентификатор листа")
    private UUID id;

    @NotNull
    @Schema(description = "Название листа")
    private String name;

    @NotNull
    @Schema(description = "Лист персонажа целиком (JSON фронтового формата)")
    private JsonNode data;
}
