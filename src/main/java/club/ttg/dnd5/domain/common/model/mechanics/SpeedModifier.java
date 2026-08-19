package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Постоянное изменение скоростей в футах.
 *
 * <p>Прибавка к ходьбе — «Подвижный» и «Проворство» (+10), «Дар скорости» (+30), «Метка
 * пути» (+5). Новые виды движения задаются либо числом («Дар совершенного полёта» — полёт
 * 40), либо флагом «равна скорости ходьбы» — так устроены «Атлет» (лазание) и «Каменные
 * крылья» (полёт).</p>
 *
 * <p>Временные ускорения — «Налётчик» на действие Рывок, «Подвижный боец» на действие
 * Атака — сюда не идут: они действуют не всегда и остаются в описании.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeedModifier {
    @Schema(description = "Прибавка к скорости ходьбы", example = "10")
    private Integer walkBonus;

    @Schema(description = "Скорость полёта", example = "40")
    private Integer fly;

    @Schema(description = "Скорость лазания")
    private Integer climb;

    @Schema(description = "Скорость плавания")
    private Integer swim;

    @Schema(description = "Скорость полёта равна скорости ходьбы")
    private Boolean flyEqualsWalk;

    @Schema(description = "Скорость лазания равна скорости ходьбы")
    private Boolean climbEqualsWalk;

    @Schema(description = "Скорость плавания равна скорости ходьбы")
    private Boolean swimEqualsWalk;
}
