package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureSkill {
    private Skill skill;
    // множитель модификатора
    private short multiplier;

    private String text;
}
