package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.SkillBonusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastSkill {
    private Skill skill;
    private SkillBonusType type;

    private String text;
}
