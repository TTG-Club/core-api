package club.ttg.dnd5.domain.beastiary.model.action;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import com.fasterxml.jackson.annotation.JsonInclude;
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
     * «Раз в N ходов»: использованное в ход T снова доступно в ход T + N, без броска.
     * Целое 2–20; {@code null} — ограничения нет. С {@link #recharge} не сочетается:
     * при заданной перезарядке не хранится (см. {@link CooldownTurns}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer cooldownTurns;

    /**
     * Боевая механика записи числами: урон, бросок атаки, спасбросок, область,
     * дистанция и накладываемые эффекты.
     *
     * <p>Не задана — в VTTG уезжает одно описание: предполагать механику по его
     * тексту выгрузка больше не пробует. Поля выше (тип атаки, спасброски, типы
     * урона) остались от старого импорта и дублируются внутри механики; тип атаки
     * и спасброски выгрузка берёт из них у записей, которым механику ещё не завели.</p>
     */
    private CreatureActionEffect effect;
}
