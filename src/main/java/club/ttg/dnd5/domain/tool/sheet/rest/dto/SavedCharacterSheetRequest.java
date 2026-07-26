package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SavedCharacterSheetRequest {

    @NotBlank
    @Schema(description = "Токен ссылки «поделиться» — последний сегмент присланного адреса листа")
    private String shareToken;
}
