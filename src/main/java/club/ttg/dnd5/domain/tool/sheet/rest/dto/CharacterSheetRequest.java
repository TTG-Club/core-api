package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetRequest {

    @Nullable
    @Size(max = 100)
    @Schema(description = "Название листа (по умолчанию — «Новый персонаж»). При обновлении null — не менять")
    private String name;

    @Nullable
    @Schema(description = "Лист персонажа целиком (JSON фронтового формата). Обязателен при создании; "
            + "при обновлении null — не менять")
    private JsonNode data;
}
