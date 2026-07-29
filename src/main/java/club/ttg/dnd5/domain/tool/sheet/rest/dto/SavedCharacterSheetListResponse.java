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

    @Schema(description = "Максимум сохранённых чужих листов у пользователя: 16, "
            + "при действующей подписке — 40")
    private int limit;

    @Schema(description = "Максимум сохранённых чужих листов, который даёт подписка. Равен limit, "
            + "если подписка уже действует — по этому равенству клиент и понимает, предлагать ли её")
    private int subscriberLimit;

    @Schema(description = "Текущее число сохранённых записей, включая ставшие недоступными")
    private int count;

    @Schema(description = "Сохранённые листы, новые первее")
    private List<SavedCharacterSheetResponse> sheets;
}
