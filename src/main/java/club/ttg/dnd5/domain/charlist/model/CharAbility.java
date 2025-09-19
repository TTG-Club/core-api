package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharAbility {
    private Ability ability;
    /**
     * Текущее значение
     */
    private byte current;
    /**
     * Бонус к характеристике
     */
    private byte bonus;
    /**
     * Владение спасбросками
     */
    private boolean savingthrow;

    public byte mod() {
        return (byte) ((current - 10) < 0 ? (current - 11) / 2 : (current - 10) / 2);
    }
}
