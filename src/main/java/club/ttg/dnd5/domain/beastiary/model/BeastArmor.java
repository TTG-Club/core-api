package club.ttg.dnd5.domain.beastiary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastArmor {
    /**
     * Класс доспеха
     */
    private byte armorClass;
    /**
     * Дополнительное описание класса доспеха (для призванных существ)
     */
    private String text;
}
