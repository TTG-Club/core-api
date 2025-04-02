package club.ttg.dnd5.domain.beastiary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastAbilities {
    /**
     * Сила
     */
    private BeastAbility strength;
    /**
     * Ловкость
     */
    private BeastAbility dexterity;
    /**
     * Телосложение
     */
    private BeastAbility constitution;
    /**
     * Интеллект
     */
    private BeastAbility intelligence;
    /**
     * Мудрость
     */
    private BeastAbility wisdom;
    /**
     * Харизма
     */
    private BeastAbility charisma;
}
