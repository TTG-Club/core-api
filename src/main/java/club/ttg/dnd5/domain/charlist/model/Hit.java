package club.ttg.dnd5.domain.charlist.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Hit {
    /**
     * Текущие
     */
    private short current;
    /**
     * Временные
     */
    private short temp;
    /**
     * Максимальное количество
     */
    private short max;

    /**
     * Количество максимальных хитов которые заменяют текущий максимум
     */
    private Short override;

    /**
     * Бонус максимальных хитов (опционально)
     */
    private Short bonus;
}
