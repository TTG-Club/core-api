package club.ttg.dnd5.domain.feat.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Прибавка к максимуму хитов.
 *
 * <p>Итог считается как
 * {@code flat + perAcquisitionLevel × уровень взятия + perLevelAfterAcquisition × (текущий
 * уровень − уровень взятия)}. Три черты справочника укладываются в эту формулу: «Дар
 * стойкости» — только {@code flat} (+40), «Крепкий» — {@code 2/2}, «Решимость синдиката» —
 * {@code 1/1}.</p>
 *
 * <p>Из-за двух последних лист обязан хранить <b>уровень, на котором черта взята</b>:
 * «Крепкий» на 4 уровне и он же на 12 дают разную прибавку навсегда.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HitPointsModifier {
    @Schema(description = "Постоянная прибавка", example = "40")
    private Integer flat;

    @Schema(description = "Прибавка за каждый уровень персонажа на момент взятия черты", example = "2")
    private Integer perAcquisitionLevel;

    @Schema(description = "Прибавка за каждый уровень, полученный после взятия черты", example = "2")
    private Integer perLevelAfterAcquisition;
}
