package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureLair {
    /**
     * Название
     */
    private String name;
    /**
     * Описание
     */
    private String description;
    /**
     * Эффекты
     */
    private Collection<CreatureAction> lairEffects;
    /**
     * Описание окончания эффектов логова
     */
    private String ending;
}
