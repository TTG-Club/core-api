package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.Ability;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Значение характеристик
 */
@Getter
@Setter
@Entity
@Table(name = "beast_abilities")
public class BeastAbility {
    @Id
    private Long id;
    /**
     * Тип характеристики
     */
    private Ability ability;
    /**
     * Значение характеристики
     */
    private short value;
    /**
     * Если истина, то для этого навыка при спасбросках добавляется бонус мастерства
     */
    private boolean save;

    /**
     * Получение модификатора характеристики
     * @return модификатор характеристики
     */
    public byte getMod() {
        return (byte) ((value - 10) < 0 ? (value - 11) / 2 : (value - 10) / 2);
    }
}
