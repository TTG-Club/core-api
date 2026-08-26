package club.ttg.dnd5.domain.background.model;

import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Владение инструментами на выбор игрока: «музыкальный инструмент на ваш выбор».
 *
 * <p>Отдельно от фиксированных инструментов ({@code Background.toolProficiencies}):
 * фиксированные предыстория выдаёт сама, а здесь она лишь очерчивает пул, и выбор
 * делается в момент, когда предысторию берут. Так же это разведено и в системе
 * ({@code BackgroundToolGrant.items} против {@code choices}).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackgroundToolChoice {
    @Schema(description = "Сколько инструментов выбирает игрок", example = "1")
    private Integer count;

    /**
     * Пул, из которого выбирают. Пустой пул означает «любой инструмент»: так предыстория
     * говорит, когда книга не сужает выбор списком.
     */
    @Schema(description = "Инструменты, из которых делается выбор")
    private List<EntityRef> from;
}
