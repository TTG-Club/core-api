package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@EqualsAndHashCode
@Getter
@Setter
public class CreatureAction {
    private String name;
    private String english;
    private String description;
    private String original;

    private AttackType attackType;
    private Collection<SawingThrow> sawingThrows;
    private Collection<DamageType> damageTypes;
    private RechargeType recharge;

    /**
     * Боевая механика записи числами: урон, бросок атаки, спасбросок, область,
     * дистанция и накладываемые эффекты.
     *
     * <p>Не задана — выгрузка в VTTG разбирает описание регулярками, как делала
     * до появления этого поля. Поля выше (тип атаки, спасброски, типы урона)
     * остались от старого импорта и дублируются внутри механики: они кормят тот
     * самый разбор у записей, которым механику ещё не завели.</p>
     */
    private CreatureActionEffect effect;
}
