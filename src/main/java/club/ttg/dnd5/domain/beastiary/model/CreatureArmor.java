package club.ttg.dnd5.domain.beastiary.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureArmor {
    /**
     * Класс доспеха
     */
    @Schema(description = "Класс доспеха", examples = "11")
    private byte armorClass;
    /**
     * Дополнительное описание класса доспеха (для призванных существ)
     */
    @Schema(description = "Дополнительное описание класса доспеха (для призванных существ)", examples = "+ уровень заклинания")
    private String text;
}
