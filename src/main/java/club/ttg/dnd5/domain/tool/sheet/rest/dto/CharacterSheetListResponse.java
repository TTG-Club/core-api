package club.ttg.dnd5.domain.tool.sheet.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetListResponse {

    @Schema(description = "Максимум активных листов у пользователя (в будущем зависит от подписки)")
    private int limit;

    @Schema(description = "Максимум удалённых листов в истории: более старые вытесняются новыми удалениями")
    private int historyLimit;

    @Schema(description = "Текущее число активных (неудалённых) листов")
    private int count;

    @Schema(description = "Листы пользователя, новые первее; у удалённых data = null")
    private List<CharacterSheetResponse> sheets;
}
