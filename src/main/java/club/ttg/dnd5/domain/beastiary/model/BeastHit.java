package club.ttg.dnd5.domain.beastiary.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastHit {
    /**
     * Среднее или абсолютное значение хитов
     */
    private Short hit;

    /**
     * Количество хит дайсов (может быть null для призванных существ)
     */
    private Short countHitDice;
    /**
     * Описание хитов если хит дайсы отсутствуют (например у призванных существ или созданных заклинанием)
     */
    private String text;
}
