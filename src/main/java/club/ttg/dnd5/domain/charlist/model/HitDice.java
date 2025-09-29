package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Dice;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HitDice {
    private Dice dice;
    /**
     * Текущее количество костей
     */
    private byte current;
    /**
     * Максимальное количество костей
     */
    private byte max;
}
