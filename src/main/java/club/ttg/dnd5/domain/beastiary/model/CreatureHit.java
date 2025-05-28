package club.ttg.dnd5.domain.beastiary.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureHit {
    /**
     * Среднее или абсолютное значение хитов
     */
    @Schema(description = "Среднее или абсолютное значение хитов")
    private Short value;

    /**
     * Количество хит дайсов (может быть null для призванных существ)
     */
    @Schema(description = "Количество хит дайсов (может быть null для призванных существ)", nullable = true)
    private Short countHitDice;
    /**
     * Описание хитов если хит дайсы отсутствуют (например у призванных существ или созданных заклинанием)
     */
    @Schema(description = "Описание хитов если хит дайсы отсутствуют (например у призванных существ или созданных заклинанием)",
            nullable = true, examples = "+ 10 за каждый уровень заклинания выше 3")
    private String text;
}
