package club.ttg.dnd5.domain.spell.model;

import club.ttg.dnd5.domain.beastiary.model.action.AttackType;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.HealingType;
import club.ttg.dnd5.domain.spell.model.enums.SpellTargetType;
import club.ttg.dnd5.domain.spell.model.enums.SpellSaveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellEffect {
    private SpellTargetType targetType;
    @Min(1)
    private Integer targetCount;
    private AreaOfEffect areaOfEffect;
    private AttackType attackType;
    private Boolean autoHit;
    private Projectiles projectiles;
    private List<String> damageFormulas;
    private List<HealingType> healingTypes;
    private List<Ability> savingThrows;
    private SpellSaveEffect saveEffect;
    private List<Condition> conditions;
}
