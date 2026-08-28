package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ступень количества выбора: с какого уровня сколько выбирают ВСЕГО.
 *
 * <p>Ступень называет итог, а не прибавку: оружейных приёмов у воина три с первого
 * уровня, четыре с четвёртого, пять с десятого и шесть с шестнадцатого. Так этот ряд
 * написан в книге, так же его показывает колонка таблицы прогрессии, и мастеру повышения
 * уровня остаётся спросить разницу с предыдущей ступенью.</p>
 *
 * <p>Отдельно от {@link CounterScaling}: там ступень — это запас зарядов, который тратят
 * и восстанавливают, а здесь — сколько всего выбрано из списка. Общее у них только имя
 * поля уровня.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceScaling {
    @Schema(description = "Уровень персонажа, с которого действует ступень", example = "4")
    private Integer level;

    @Schema(description = "Сколько всего выбрано к этому уровню", example = "4")
    private Integer count;
}
