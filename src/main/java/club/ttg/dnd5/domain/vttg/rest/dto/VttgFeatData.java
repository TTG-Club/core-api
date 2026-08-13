package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * «Дары» черты в том виде, в каком их применяет лист персонажа VTTG
 * ({@code GameItem.featData}).
 *
 * <p>Это ПОЛНЫЙ контракт потребителя: он читает именно это поле и по нему проставляет
 * владения, защиты, модификаторы, требования и спрашивает выборы. Всё, что его типы
 * выражают, лежит здесь — и больше нигде, чтобы одни и те же данные не ехали дважды.</p>
 *
 * <p>То, чего форма листа не выражает, остаётся в {@link VttgFeatMechanics}: варианты
 * повышения характеристик «или/или», владение инструментами и смена типа существа.
 * Дублирования между блоками нет.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeatData {
    /** Дискриминант блоба — всегда "feat". */
    private String type;
    /** Владение навыками (camelCase-слаги). */
    private List<String> skillProficiencies;
    /** Владение доспехами: {@code light}/{@code medium}/{@code heavy}/{@code shield}. */
    private List<String> armorProficiencies;
    /** Владение оружием: {@code simple}/{@code martial}. */
    private List<String> weaponProficiencies;
    /** Защиты от типов урона. */
    private List<DamageDefense> damageDefenses;
    /** Иммунитеты к состояниям. */
    private List<String> conditionImmunities;
    /** Тёмное зрение в футах — единственное чувство, влияющее на зрение токена. */
    private Integer darkvision;
    /**
     * Повышение характеристик. Заполняется, только когда вариант ОДИН: форма
     * потребителя описывает один выбор, а «+2 к одной либо +1 к двум» — это два
     * взаимоисключающих варианта, и они целиком лежат в {@code mechanics.abilityBonuses}.
     */
    private AbilityScoreIncrease abilityScoreIncrease;
    /** Постоянные модификаторы листа: хиты, скорости, КД, чувства, инициатива. */
    private Modifiers modifiers;
    /** Выборы, которые игрок делает при взятии черты. */
    private List<VttgFeatMechanics.Choice> choices;
    /** Разобранное предварительное условие. */
    private VttgFeatPrerequisite prerequisite;

    /**
     * Защита от типа урона.
     *
     * @param damageType тип урона
     * @param kind       {@code resistance}, {@code immunity} или {@code vulnerability}
     */
    public record DamageDefense(String damageType, String kind) {
    }

    /**
     * Постоянные модификаторы листа в форме потребителя.
     *
     * <p>Уже, чем модификаторы источника: защиты от урона едут в
     * {@link VttgFeatData#damageDefenses}, иммунитеты к состояниям — в
     * {@link VttgFeatData#conditionImmunities}, тёмное зрение — в
     * {@link VttgFeatData#darkvision}, а смена типа существа листу пока не по силам
     * и остаётся в {@code mechanics}.</p>
     *
     * @param hitPoints                  прибавка к максимуму хитов
     * @param speed                      изменение скоростей
     * @param armorClassBonus            постоянная прибавка к КД
     * @param senses                     чувства с дистанцией (без тёмного зрения)
     * @param telepathyRange             дальность телепатии в футах
     * @param resistanceFromChoiceKey    ключ выбора типа урона, к которому даётся
     *                                   сопротивление: сам тип известен только после выбора
     * @param initiativeProficiencyBonus к инициативе прибавляется бонус мастерства
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Modifiers(VttgFeatMechanics.HitPoints hitPoints,
                            VttgFeatMechanics.Speed speed,
                            Integer armorClassBonus,
                            List<VttgFeatMechanics.Sense> senses,
                            Integer telepathyRange,
                            String resistanceFromChoiceKey,
                            Boolean initiativeProficiencyBonus) {
    }

    /**
     * Повышение характеристик на выбор: {@code +amount} к {@code count} характеристикам
     * из набора {@code from} (пусто — любая).
     *
     * @param choice        описание выбора
     * @param fromChoiceKey ключ ранее сделанного выбора, к которому привязано повышение
     *                      («Устойчивый» поднимает ту характеристику, спасбросками
     *                      которой овладел)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AbilityScoreIncrease(Choice choice, String fromChoiceKey) {

        /**
         * @param amount на сколько поднимается каждая выбранная характеристика
         * @param count  сколько характеристик выбирают
         * @param from   характеристики на выбор; пусто — любая
         */
        public record Choice(Integer amount, Integer count, List<String> from) {
        }
    }
}
