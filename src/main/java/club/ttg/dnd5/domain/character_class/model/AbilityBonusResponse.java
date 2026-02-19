package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AbilityBonusResponse {
    private int level;
    private List<Ability> abilities;
    private Integer bonus;
    private Integer upto;
}
