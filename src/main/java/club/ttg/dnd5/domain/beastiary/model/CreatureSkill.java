package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureSkill {
    private Skill skill;
    @Schema(description = "множитель модификатора")
    private short multiplier;
    @Schema(description = "дополнительный бонус")
    private Short bonus;
    private String text;
}
