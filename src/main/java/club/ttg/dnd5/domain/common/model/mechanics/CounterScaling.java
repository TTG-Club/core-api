package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ступень максимума ресурса: с какого уровня сколько зарядов.
 *
 * <p>Ступенями, а не формулой, потому что формулой такое не пишется: кости превосходства
 * мастера боевых искусств — это 4 с третьего уровня, 5 с седьмого и 6 с пятнадцатого, и
 * ни бонус мастерства, ни уровень такой ряд не повторяют.</p>
 *
 * <p>Ровно так же устроена колонка таблицы прогрессии ({@code ClassTableColumn.scaling}):
 * ресурс с колонкой уже описан ею, и второй формы записи для того же ему не нужно —
 * ступени заводят ресурсу БЕЗ колонки.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CounterScaling {
    @Schema(description = "Уровень персонажа, с которого действует ступень", example = "7")
    private Integer level;

    @Schema(description = "Максимум зарядов на этой ступени", example = "5")
    private Integer max;
}
