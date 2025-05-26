package club.ttg.dnd5.domain.beastiary.rest.dto;

import club.ttg.dnd5.domain.beastiary.model.CreatureHit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link CreatureHit}
 */
@Getter
@Setter
public class HitResponse {
    @Schema(description = "Среднее количество хитов или абсолютное значение")
    private Short hit;
    @Schema(description = "формула расчета хитов")
    private String formula;
    @Schema(description = "Дополнительный текст для призванных существ")
    private String text;
}