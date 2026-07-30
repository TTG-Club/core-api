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

    @Schema(description = "Максимум активных листов у пользователя: 8, при действующей подписке — 20")
    private int limit;

    @Schema(description = "Максимум активных листов, который даёт подписка. Равен limit, если "
            + "подписка уже действует — по этому равенству клиент и понимает, предлагать ли её")
    private int subscriberLimit;

    @Schema(description = "Максимум удалённых листов в истории (20, при действующей подписке — 30): "
            + "более старые вытесняются новыми удалениями")
    private int historyLimit;

    @Schema(description = "Глубина истории удалённых, которую даёт подписка")
    private int subscriberHistoryLimit;

    @Schema(description = "Текущее число активных (неудалённых) листов")
    private int count;

    @Schema(description = "Листы пользователя, новые первее; у удалённых data = null")
    private List<CharacterSheetResponse> sheets;
}
