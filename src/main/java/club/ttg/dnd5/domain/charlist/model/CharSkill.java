package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharSkill {
    private String name;
    private Ability ability;

    /**
     * Значение умения, если указан вручную
     */
    private Byte value;

    /**
     * Множитель умения: 0 нет, 1 есть, 2 компетенция
     */
    private byte multiplier;
    private byte bonus;
}
