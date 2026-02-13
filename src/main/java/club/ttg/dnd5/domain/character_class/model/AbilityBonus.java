package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AbilityBonus {
    private List<Ability> abilities;
    private Integer bonus;
    private Integer upto;
}
