package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureAction {
    private String name;
    private String english;
    private String description;

    private AttackType attackType;
    private Collection<SawingThrow> sawingThrows;
    private Collection<DamageType> damageTypes;
    private RechargeType recharge;
}
