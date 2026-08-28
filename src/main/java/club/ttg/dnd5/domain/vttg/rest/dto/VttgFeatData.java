package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * «Дары» черты в том виде, в каком их применяет лист персонажа VTTG
 * ({@code GameItem.featData}).
 *
 * <p>Это ПОЛНЫЙ контракт потребителя: он читает именно это поле и по нему проставляет
 * владения, защиты, модификаторы, требования и спрашивает выборы. Всё, что его типы
 * выражают, лежит здесь — и больше нигде, чтобы одни и те же данные не ехали дважды.</p>
 *
 * <p>То, чего форма листа не выражает, остаётся в {@link VttgFeatMechanics}: варианты
 * повышения характеристик «или/или», смена типа существа и те владения-ссылки, которым не
 * нашлось ключа в справочнике листа. Дублирования между блоками нет — единственный вариант
 * повышения едет только сюда, а список вариантов только туда.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeatData {
    /** Дискриминант блоба — всегда "feat". */
    private String type;
    /** Владение навыками (camelCase-слаги). */
    private List<String> skillProficiencies;
    /** Владение спасбросками (слаги характеристик: {@code constitution}). */
    private List<String> savingThrowProficiencies;
    /** Владение доспехами: {@code light}/{@code medium}/{@code heavy}/{@code shield}. */
    private List<String> armorProficiencies;
    /**
     * Владение оружием: категория ({@code simple}/{@code martial}) либо конкретный вид
     * ({@code longsword}). Потребитель принимает оба — «Мастер оружия» даёт владение
     * именно видом.
     */
    private List<String> weaponProficiencies;
    /**
     * Оружейные приёмы (weapon mastery, 2024) — ключами видов оружия.
     *
     * <p>Отдельным списком от {@link #weaponProficiencies}, потому что на листе это
     * отдельный раздел владений ({@code proficiencies.weaponMasteries}), а не подмножество
     * владения оружием. «Мастер оружия» даёт и то, и другое.</p>
     */
    private List<String> weaponMasteries;
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
    /**
     * Защиты от типов урона, которые называет игрок: сам тип известен только после
     * ответа на выбор из {@link #choices}, поэтому в {@link #damageDefenses} такая защита
     * лечь не может.
     */
    private List<DamageDefenseChoice> damageDefenseChoices;
    /** Иммунитеты к состояниям. */
    private List<String> conditionImmunities;
    /** Тёмное зрение в футах — единственное чувство, влияющее на зрение токена. */
    private Integer darkvision;
    /**
     * Повышение характеристик. Заполняется, только когда вариант ОДИН: форма
     * потребителя описывает одно повышение, а «+2 к одной либо +1 к двум» — это два
     * взаимоисключающих варианта, и они целиком лежат в {@code mechanics.abilityBonuses}.
     *
     * <p>Готовая прибавка и прибавка на выбор различаются внутри — см.
     * {@link AbilityScoreIncrease}.</p>
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
     * Заклинания, которые черта добавляет в список заклинаний класса — таблица
     * «Заклинания метки». Не выдача: подготовку и ячейку на них персонаж тратит сам,
     * поэтому отдельным полем от {@link #grantedSpells}.
     */
    private SpellList spellList;
    /** Ресурсы черты со счётчиком: очки удачи «Удачливого», применения «Целителя». */
    private List<Counter> counters;
    /**
     * Черты, которые запись выдаёт без выбора, — ссылками на записи компендиума. Выдаёт
     * их умение класса; лист кладёт черту сам, как предыстория кладёт черту происхождения.
     */
    private List<GrantedFeat> grantedFeats;

    /**
     * Черта, выданная без выбора.
     *
     * @param featId {@code id} записи черты в выгрузке ({@code srd_feat_…})
     * @param name   название на момент выгрузки — показать, даже когда записи в паках нет
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GrantedFeat(String featId, String name) {
    }

    /**
     * Заклинание, которое черта даёт знать.
     *
     * <p>Форма та же, что у врождённых заклинаний вида ({@code VttgSpecies.GrantedSpell}):
     * потребитель ищет запись по {@code spellId}, а {@code name} показывает, даже когда
     * такой записи в паках нет. Круг и школа не дублируются — они берутся из самой записи
     * заклинания и в снимке разошлись бы с каталогом.</p>
     *
     * <p>{@code requiredLevel} — уровень персонажа, с которого заклинание доступно: у
     * метки дракона «Лечение ран» есть сразу, а «Малое восстановление» приходит на
     * третьем. Пусто — с момента взятия черты. Без уровня лист выдал бы весь список сразу,
     * и черта на первом уровне оказалась бы сильнее книжной.</p>
     *
     * @param name          название заклинания на момент сохранения
     * @param spellId       {@code id} записи заклинания в выгрузке (он же {@code url} на сайте)
     * @param requiredLevel уровень персонажа, с которого заклинание доступно
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GrantedSpell(String name, String spellId, Integer requiredLevel) {
    }

    /**
     * Заклинание, добавленное чертой в список заклинаний класса.
     *
     * <p>Своим типом, а не {@link GrantedSpell}: то заклинание персонаж знает и
     * накладывает, а это он лишь может подготовить наравне с классовыми. Уровня здесь нет
     * намеренно — доступность определяет круг самого заклинания и ячейки персонажа, а круг
     * лист берёт из записи компендиума.</p>
     *
     * @param name    название заклинания на момент сохранения
     * @param spellId {@code id} записи заклинания в выгрузке (он же {@code url} на сайте)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpellListSpell(String name, String spellId) {
    }

    /**
     * Один список расширения — со своим уровнем доступа и своим количеством.
     *
     * <p>Списков у черты может быть несколько, и это НЕ взаимоисключающие варианты: каждый
     * открывается на своём уровне и складывается с предыдущими. Без разбивки лист открыл бы
     * всю таблицу на первом уровне.</p>
     *
     * @param requiredLevel уровень персонажа, с которого список открывается; пусто — сразу
     * @param count         сколько заклинаний берут: число либо выражение с {@code @prof},
     *                      {@code @level}, {@code @mod.<abbr>}; пусто — весь список
     * @param spells        заклинания списка
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpellListGroup(Integer requiredLevel, String count, List<SpellListSpell> spells) {
    }

    /**
     * Расширение списка заклинаний класса — таблица «Заклинания метки».
     *
     * @param groups               списки заклинаний по уровням доступа
     * @param requiresSpellcasting нужно умение «Использование заклинаний» или «Магия
     *                             договора»; пусто — расширяет всегда
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpellList(List<SpellListGroup> groups, Boolean requiresSpellcasting) {
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
     * Защита от типа урона, который называет игрок.
     *
     * @param choiceKey ключ выбора из {@link VttgFeatData#choices}, ответ на который даёт
     *                  тип урона
     * @param kind      {@code resistance}, {@code immunity} или {@code vulnerability}
     */
    public record DamageDefenseChoice(String choiceKey, String kind) {
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
     * @param resistanceFromChoiceKey    легаси-поле: ключ выбора типа урона, к которому
     *                                   даётся сопротивление. Дублирует первую запись
     *                                   {@link VttgFeatData#damageDefenseChoices} с видом
     *                                   {@code resistance} — там же лежат иммунитет и
     *                                   уязвимость по выбору, которых это поле не знает
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
     * Ресурс черты со счётчиком.
     *
     * <p>Уже классового счётчика ({@code VttgClass.Counter}): у черты нет ни уровня
     * начала, ни прогрессии по уровням — она либо взята, либо нет.</p>
     *
     * @param key       стабильный ключ ресурса в пределах черты
     * @param name      название на листе («Очки удачи»)
     * @param shortName краткое название для компактной плитки; пусто — плитка подпишется
     *                  полным
     * @param max       формула максимума: число либо выражение с {@code @prof},
     *                  {@code @level}, {@code @mod.<abbr>}
     * @param recovery  каким отдыхом восстанавливается: {@code short} или {@code long}
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Counter(String key, String name, String shortName, String max, String recovery) {
    }

    /**
     * Повышение характеристик.
     *
     * <p>Два взаимоисключающих вида. {@link #fixed} — готовая прибавка, которую лист
     * ставит сам при перетаскивании черты. {@link #choice} — прибавка на выбор: лист её
     * НЕ применяет, пока не знает выбранного, и без {@link #fromChoiceKey} она остаётся
     * подсказкой в сводке даров. Поэтому черта, у которой выбирать нечего («Крепкий» —
     * всегда +1 Телосложения), едет именно {@code fixed}: иначе прибавка не встала бы.</p>
     *
     * @param fixed         готовая прибавка: характеристика → на сколько поднять
     * @param choice        прибавка на выбор
     * @param fromChoiceKey ключ ранее сделанного выбора, из ответа на который берётся
     *                      характеристика («Устойчивый» поднимает ту, спасбросками которой
     *                      овладел). Без него лист повышение по выбору не применяет
     * @param upto          предел, выше которого характеристику не поднять: 20 у черт,
     *                      30 у эпических даров — тем они и отличаются
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AbilityScoreIncrease(Map<String, Integer> fixed, Choice choice,
                                       String fromChoiceKey, Integer upto) {

        /**
         * @param amount на сколько поднимается каждая выбранная характеристика
         * @param count  сколько характеристик выбирают
         * @param from   характеристики на выбор; пусто — любая
         */
        public record Choice(Integer amount, Integer count, List<String> from) {
        }
    }
}
