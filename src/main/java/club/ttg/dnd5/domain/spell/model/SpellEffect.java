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
    /**
     * Цели частей урона по индексам {@link #damageFormulas}: {@code selected}
     * (дефолт), {@code self}, {@code choose} — словарь VTTG {@code DamagePartTarget}.
     * В формулу цель не пишется: VTTG знает в ней только теги
     * {@code @target.full}/{@code @target.notFull}.
     */
    private List<String> damageFormulaTargets;
    /**
     * Признак «применять часть, только если по цели фактически нанесён урон»
     * по индексам {@link #damageFormulas} — словарь VTTG
     * {@code DamagePart.requiresDamage}. Покрывает «лечусь, только если задел
     * врага»: часть лечения гасится, когда урон не прошёл.
     */
    private List<Boolean> damageFormulaRequiresDamage;
    private List<HealingType> healingTypes;
    /**
     * Характеристика, от которой считаются Сл спасброска и бонус атаки этого
     * заклинания.
     *
     * <p>Обычно её задаёт заклинатель, а не запись справочника: «Огненный снаряд»
     * считается от Интеллекта у волшебника и от Харизмы у колдуна. Поле нужно
     * тем заклинаниям, у которых характеристика своя независимо от источника —
     * хоумбрю и заклинаниям, выданным чертой. Не задано — характеристику берёт
     * потребитель у класса.</p>
     */
    private Ability spellcastingAbility;
    private List<Ability> savingThrows;
    private SpellSaveEffect saveEffect;
    private List<Condition> conditions;
}
