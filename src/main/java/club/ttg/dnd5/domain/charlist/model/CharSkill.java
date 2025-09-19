package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharSkill {
    private Skill skill;
    private byte value;
    private byte bonus;
}
