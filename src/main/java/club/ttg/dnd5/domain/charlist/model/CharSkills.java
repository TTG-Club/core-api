package club.ttg.dnd5.domain.charlist.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CharSkills {
    private CharSkill acrobatics;
    private CharSkill animalHandling;
    private CharSkill arcana;
    private CharSkill athletics;
    private CharSkill deception;
    private CharSkill history;
    private CharSkill insight;
    private CharSkill intimidation;
    private CharSkill investigation;
    private CharSkill medicine;
    private CharSkill nature;
    private CharSkill perception;
    private CharSkill performance;
    private CharSkill persuasion;
    private CharSkill religion;
    private CharSkill sleightOfHand;
    private CharSkill stealth;
    private CharSkill survival;

    private Collection<CharSkill> custom;
}
