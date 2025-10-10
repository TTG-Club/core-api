package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Armor {
    /**
     * Категория доспеха
     */
    private ArmorCategory category;
    /**
     *  КД.
     */
    private Integer armorClass;
    /**
     * Добавление модификатора Ловкости к классу доспеха
     */
    private DexterityMod mod;
    /** Сила. */
    private String strength;
    /** Скрытность. */
    private Boolean stealth;

    @AllArgsConstructor
    @Getter
    public enum DexterityMod {
        PLUS ("+ модификатор Ловкости"),
        PLUS_MAX_2("+ модификатор Ловкости (максимум +2)"),
        NONE("");

        private final String name;
    }
}
