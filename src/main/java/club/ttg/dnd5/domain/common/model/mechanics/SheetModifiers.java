package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Постоянные модификаторы листа персонажа: то, что источник эффекта меняет в шапке и в
 * блоке защит раз и навсегда, без условий и активации.
 *
 * <p>Общий блок для всего, что наделяет персонажа постоянными свойствами: черты
 * ({@code FeatMechanics}) и умения вида ({@code SpeciesMechanics}). Лист применяет
 * их одинаково, поэтому и модель одна — как {@code AbilityBonus}, который переехал сюда,
 * когда понадобился второму потребителю.</p>
 *
 * <p>Модель типизированная, а не {@code ActiveEffect} в вокабуляре VTTG, которым описаны
 * магические предметы и заклинания. Причины три: редактор получает форму с выпадающими
 * списками вместо строковых ключей вроде {@code movement.walk}; прибавка к максимуму хитов
 * у «Крепкого» зависит от уровня взятия черты и в паре «ключ — значение» не выражается;
 * сопротивление по выбору ссылается на {@code mechanics.choices}. Перевод в вокабуляр
 * VTTG — задача мапперов экспорта.</p>
 *
 * <p>Условные эффекты сюда не попадают: «Оборона» даёт +1 к КД только в доспехе,
 * «Дар духа ночи» — сопротивление только в темноте, полёт гарпии не работает в средних и
 * тяжёлых доспехах. Условие пришлось бы описывать отдельным языком, а лист всё равно
 * показывает такие эффекты справкой. Они остаются в описании.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SheetModifiers {
    @Schema(description = "Прибавка к максимуму хитов")
    private HitPointsModifier hitPoints;

    @Schema(description = "Изменение скоростей")
    private SpeedModifier speed;

    /**
     * Постоянная прибавка к КД. У «Обороны» она есть только в доспехе — само условие
     * остаётся в описании, потому что лист всё равно знает, надет ли доспех.
     */
    @Schema(description = "Прибавка к классу доспеха", example = "1")
    private Integer armorClassBonus;

    @Schema(description = "Чувства с дистанцией")
    private List<SenseGrant> senses;

    /**
     * Дальность телепатии в футах («Дар Общения» — 120). Отдельным полем, а не в
     * {@link #senses}: телепатия не чувство и в {@code SenseType} её нет.
     */
    @Schema(description = "Дальность телепатии в футах", example = "120")
    private Integer telepathyRange;

    @Schema(description = "Сопротивления, иммунитеты и уязвимости к урону")
    private DamageAffinity damage;

    /**
     * Иммунитет к состояниям. Покрывает «Дар мастера ядов» (отравленный); иммунитет к
     * одержимости у «Дара непоколебимой преданности» сюда не попадает — такого состояния
     * в справочнике нет, и ради одной черты словарь не расширяется.
     */
    @Schema(description = "Иммунитет к состояниям")
    private Set<Condition> conditionImmunities;

    /**
     * Тип существа, если черта его меняет: «Вознесение лича» и «Вознесение рыцаря смерти»
     * делают персонажа нежитью, а это меняет применимость эффектов на всём листе.
     */
    @Schema(description = "Новый тип существа", example = "UNDEAD")
    private CreatureType creatureType;

    /**
     * К броску инициативы прибавляется бонус мастерства — «Бдительный». Преимущество на
     * инициативу («Проворство») это поле не описывает: преимущество не число.
     */
    @Schema(description = "Бонус мастерства прибавляется к инициативе")
    private Boolean initiativeProficiencyBonus;
}
