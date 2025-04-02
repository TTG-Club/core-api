package club.ttg.dnd5.domain.beastiary.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link club.ttg.dnd5.domain.beastiary.model.BeastHit}
 */
@Getter
@Setter
public class BeastHitDto {
    @Schema(description = "Среднее количество хитов или абсолютное значение")
    private Short hit;
    @Schema(description = "формула расчета хитов")
    private String formula;
    @Schema(description = "Дополнительный текст для призванных существ")
    private String text;
}