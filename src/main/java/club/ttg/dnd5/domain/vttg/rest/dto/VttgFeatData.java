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
 * повышения характеристик «или/или», смена типа существа и те инструменты, которым не
 * нашлось ключа в справочнике листа. Дублирования между блоками нет.</p>
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
    /**
     * Владение инструментами — ключами справочника листа ({@code thieves-tools}), а не
     * слагами страниц сайта: незнакомый ключ лист молча выбрасывает при первом же
     * открытии окна владений. Перевод — {@code VttgToolKeys}; инструменты, которых у
     * листа нет, остаются ссылками в {@code mechanics.proficiencies}.
     */
    private List<String> toolProficiencies;
    /**
     * Известные языки — РУССКИМИ названиями из справочника листа: ключей у языков там нет,
     * владение хранится подписью.
     */
    private List<String> languages;
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
     * Заклинания, которые черта даёт знать без выбора. Выбираемые заклинания сюда не идут:
     * у них есть количество и фильтр, и живут они в {@link #choices}.
     */
    private List<GrantedSpell> grantedSpells;
    /**
     * Заклинательная характеристика заклинаний черты ({@code intelligence}/{@code wisdom}/
     * {@code charisma}). Пусто — черта её не задаёт: либо характеристику выбирает игрок
     * (выбор типа {@code spellcastingAbility}), либо она берётся от класса.
     */
    private String spellcastingAbility;
    /**
     * Выданные чертой заклинания не нужно готовить. Пусто читается как «готовить нужно»:
     * заклинание ложится в книгу наравне с остальными.
     */
    private Boolean grantedSpellsAlwaysPrepared;

    /**
     * Заклинание, которое черта даёт знать.
     *
     * <p>Форма та же, что у врождённых заклинаний вида ({@code VttgSpecies.GrantedSpell}):
     * потребитель ищет запись по {@code spellId}, а {@code name} показывает, даже когда
     * такой записи в паках нет. Круг и школа не дублируются — они берутся из самой записи
     * заклинания и в снимке разошлись бы с каталогом.</p>
     *
     * @param name    название заклинания на момент сохранения
     * @param spellId {@code id} записи заклинания в выгрузке (он же {@code url} на сайте)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GrantedSpell(String name, String spellId) {
    }

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
     * @param initiativeBonus            постоянная числовая прибавка к инициативе;
     *                                   складывается с {@code initiativeProficiencyBonus},
     *                                   если стоят оба
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Modifiers(VttgFeatMechanics.HitPoints hitPoints,
                            VttgFeatMechanics.Speed speed,
                            Integer armorClassBonus,
                            List<VttgFeatMechanics.Sense> senses,
                            Integer telepathyRange,
                            String resistanceFromChoiceKey,
                            Boolean initiativeProficiencyBonus,
                            Integer initiativeBonus) {
    }

    /**
     * Повышение характеристик на выбор: {@code +amount} к {@code count} характеристикам
     * из набора {@code from} (пусто — любая).
     *
     * @param choice        описание выбора
     * @param fromChoiceKey ключ ранее сделанного выбора, к которому привязано повышение
     *                      («Устойчивый» поднимает ту характеристику, спасбросками
     *                      которой овладел)
     * @param upto          предел, выше которого характеристику не поднять: 20 у черт,
     *                      30 у эпических даров — тем они и отличаются
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AbilityScoreIncrease(Choice choice, String fromChoiceKey, Integer upto) {

        /**
         * @param amount на сколько поднимается каждая выбранная характеристика
         * @param count  сколько характеристик выбирают
         * @param from   характеристики на выбор; пусто — любая
         */
        public record Choice(Integer amount, Integer count, List<String> from) {
        }
    }
}
