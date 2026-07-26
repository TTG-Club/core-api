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
public class SavedCharacterSheetListResponse {

    @Schema(description = "Максимум сохранённых чужих листов у пользователя (в будущем зависит от подписки)")
    private int limit;

    @Schema(description = "Текущее число сохранённых записей, включая ставшие недоступными")
    private int count;

    @Schema(description = "Сохранённые листы, новые первее")
    private List<SavedCharacterSheetResponse> sheets;
}
