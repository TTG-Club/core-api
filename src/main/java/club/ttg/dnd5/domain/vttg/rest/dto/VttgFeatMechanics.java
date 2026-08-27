package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Механика черты, которую форма листа не выражает.
 *
 * <p>Дополняет {@link VttgFeatData}, а НЕ дублирует его: всё, что потребитель умеет
 * применять, лежит там. Здесь остаётся только то, чему в его типах места нет — иначе
 * одни и те же данные ехали бы в записи дважды и разошлись бы при первой же правке.</p>
 *
 * <p>Остатка ровно три вида, и у каждого своя причина:</p>
 * <ul>
 *   <li>{@link #abilityBonuses} — варианты повышения «или/или». У потребителя
 *       {@code abilityScoreIncrease} описывает ОДНО повышение, а «+2 к одной либо +1 к
 *       двум» — это выбор между двумя разными повышениями, и вторым полем его не задать.
 *       Единственный вариант сюда не едет: он целиком укладывается в
 *       {@code featData.abilityScoreIncrease};</li>
 *   <li>{@link #proficiencies} — владения, которым не нашлось ключа в справочнике листа.
 *       Незнакомый ключ лист молча выбрасывает при первом же открытии окна владений,
 *       поэтому такое владение отдаётся ссылкой: применить его нельзя, но видно будет,
 *       и карточка записи откроется;</li>
 *   <li>{@link #creatureType} — смена типа существа. На листе тип существа не свойство
 *       персонажа, и менять его пока нечем.</li>
 * </ul>
 *
 * <p>Всё остальное — владения, защиты, модификаторы, выборы, ресурсы, требования — лист
 * применяет сам, и живёт оно в {@link VttgFeatData}.</p>
 *
 * <p>Словарь — потребителя: характеристики и навыки слагами, категории ключами.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeatMechanics {
    /**
     * Варианты повышения характеристик — ТОЛЬКО когда их больше одного:
     * «Улучшение характеристик» — это {@code +2 к одной} либо {@code +1 к двум}.
     * Единственный вариант сюда не попадает: он целиком уехал в
     * {@code featData.abilityScoreIncrease}, и второй копией разошёлся бы с ним при
     * первой же правке.
     */
    private List<AbilityBonus> abilityBonuses;
    /**
     * Владения, которым не нашлось ключа в справочнике листа. Те, что нашлись, уезжают
     * ключами в {@code featData} и применяются сами; сюда попадает остаток — ссылкой,
     * чтобы владение было хотя бы видно и открывалось карточкой.
     */
    private ProficiencyGrant proficiencies;
    /**
     * Смена типа существа («Вознесение лича» делает персонажа нежитью). Лист такого
     * пока не умеет — поле отдаётся ради полноты записи.
     */
    private String creatureType;

    /**
     * Повышение характеристик: из {@code abilities} игрок берёт {@code count} штук и
     * поднимает каждую на {@code bonus}, но не выше {@code upto}.
     *
     * @param abilities     характеристики на выбор
     * @param bonus         на сколько поднимается каждая выбранная
     * @param upto          предел (20 у черт, 30 у эпических даров)
     * @param count         сколько характеристик выбирают
     * @param fromChoiceKey ключ ранее сделанного выбора, к которому привязано повышение
     *                      («Устойчивый» поднимает ту характеристику, спасбросками
     *                      которой овладел)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AbilityBonus(List<String> abilities, Integer bonus, Integer upto,
                               Integer count, String fromChoiceKey) {
    }

    /**
     * Владения, выданные без выбора, которых нет в справочнике листа — ссылками на записи
     * сайта.
     *
     * <p>Инструменты и оружие лист хранит ключами своего закрытого справочника
     * ({@code thieves-tools}, {@code longsword}) и незнакомый ключ молча выбрасывает.
     * Поэтому владение записью, которой у листа нет, применить нечем — но и потерять его
     * нельзя, и оно отдаётся ссылкой.</p>
     *
     * @param tools           инструменты без ключа справочника листа
     * @param weapons         виды оружия без ключа справочника листа
     * @param weaponMasteries оружейные приёмы, чьего вида оружия у листа нет
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProficiencyGrant(List<VttgEntityRef> tools, List<VttgEntityRef> weapons,
                                   List<VttgEntityRef> weaponMasteries) {
    }

    /**
     * Прибавка к максимуму хитов.
     *
     * <p>Итог: {@code flat + perAcquisitionLevel × уровень взятия +
     * perLevelAfterAcquisition × (текущий уровень − уровень взятия)}. Из-за двух
     * последних слагаемых потребитель обязан помнить уровень, на котором черту взяли.</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HitPoints(Integer flat, Integer perAcquisitionLevel,
                            Integer perLevelAfterAcquisition) {
    }

    /**
     * Постоянное изменение скоростей в футах. Новый вид движения задаётся либо числом,
     * либо флагом «равна скорости ходьбы».
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Speed(Integer walkBonus, Integer fly, Integer climb, Integer swim,
                        Boolean flyEqualsWalk, Boolean climbEqualsWalk, Boolean swimEqualsWalk) {
    }

    /**
     * Чувство с дистанцией.
     *
     * @param type  {@code blindsight}, {@code truesight}, {@code tremorsense}
     * @param range дистанция в футах
     */
    public record Sense(String type, Integer range) {
    }


    /**
     * Выбор, который игрок делает при взятии черты.
     *
     * @param key                        стабильный ключ выбора в пределах черты
     * @param type                       что выбирают: {@code skill}, {@code tool},
     *                                   {@code language}, {@code damageType}, {@code spell},
     *                                   {@code cantrip}, {@code spellList},
     *                                   {@code spellcastingAbility}, {@code weapon},
     *                                   {@code weaponMastery}, {@code armor},
     *                                   {@code ability}, {@code savingThrow}, {@code option}.
     *                                   При смешанном наборе — первый вид набора
     * @param types                      полный набор видов, когда выбирают из нескольких
     *                                   справочников сразу («Умелый» — навык ИЛИ
     *                                   инструмент). Задан только при нескольких видах;
     *                                   куда лечь выбранному, решает принадлежность самого
     *                                   значения
     * @param label                      подпись для игрока
     * @param count                      сколько значений выбирают
     * @param countEqualsProficiencyBonus количество равно бонусу мастерства
     * @param options                    допустимые значения; пусто — любое своего типа
     * @param spellFilter                ограничение выбора заклинания
     * @param onlyIfNotProficient        только то, чем персонаж ещё не владеет
     * @param onlyIfProficient           только то, чем персонаж уже владеет
     * @param expertiseIfProficient      владеет выбранным — получает компетентность
     * @param grants                     что даёт выбор: {@code proficiency}/{@code expertise}
     * @param rechooseOnLongRest         выбор пересматривается на продолжительном отдыхе
     * @param requiredLevel              уровень, с которого выбор открывается; пусто —
     *                                   сразу. По нему мастер повышения спрашивает
     *                                   компетентность плута и на первом уровне, и на
     *                                   шестом, где умение в книге одно
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Choice(String key, String type, List<String> types, String label,
                         Integer count, Boolean countEqualsProficiencyBonus,
                         List<Option> options, SpellFilter spellFilter,
                         Boolean onlyIfNotProficient, Boolean onlyIfProficient,
                         Boolean expertiseIfProficient, String grants,
                         Boolean rechooseOnLongRest, Integer requiredLevel) {
    }

    /**
     * Допустимое значение выбора — в СЛОВАРЕ ПОТРЕБИТЕЛЯ, а не источника.
     *
     * <p>Значение потребитель кладёт прямо во владения актора, поэтому оно переводится по
     * типу выбора: навык — слагом ({@code sleightOfHand}), характеристика и тип урона —
     * ключом ({@code charisma}, {@code fire}), язык — русским названием справочника листа,
     * инструмент — ключом владения ({@code thieves-tools}), список заклинаний — ключом
     * класса ({@code wizard}), оружие и оружейный приём — ключом вида оружия
     * ({@code longsword}), доспехи — категорией ({@code medium}). Заклинание и «вариант»
     * остаются как есть: у первого значение и так url записи, у второго общего словаря
     * нет.</p>
     *
     * <p>У смешанного выбора вид значения решает не {@code type}, а сам справочник: перевод
     * пробуется по каждому виду набора, пока не найдётся тот, где значение есть.</p>
     *
     * @param value значение в словаре своего типа
     * @param name  подпись для игрока
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Option(String value, String name) {
    }

    /**
     * Чем ограничен выбор заклинания.
     *
     * @param level                точный уровень; 0 — заговор
     * @param maxLevel             наибольший допустимый уровень
     * @param schools              школы магии
     * @param classes              классы, из списков которых можно выбирать — ссылками на
     *                             записи, для показа
     * @param classKeys            те же классы каноническими ключами ({@code wizard}): по
     *                             ним потребитель собирает пул, сверяя со
     *                             {@code spell.classKeys}. Слаг страницы для этого не годится —
     *                             он несёт суффикс источника ({@code wizard-phb})
     * @param classesFromChoiceKey ключ выбора, из ответа на который берётся класс:
     *                             «Посвящённый в магию» сперва спрашивает список — жреца,
     *                             друида или волшебника, — и только потом даёт выбрать из
     *                             него заговоры
     * @param castingTime          время накладывания ({@code ritual}, {@code action}, …)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpellFilter(Integer level, Integer maxLevel, List<String> schools,
                              List<VttgEntityRef> classes, List<String> classKeys,
                              String classesFromChoiceKey, String castingTime) {
    }
}
