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
 * <p>Словарь — потребителя: характеристики и навыки слагами, категории ключами.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeatMechanics {
    /**
     * Варианты повышения характеристик. Несколько элементов — это выбор «или/или»:
     * «Улучшение характеристик» — это {@code +2 к одной} либо {@code +1 к двум}. Форма
     * листа описывает один выбор, поэтому единственный вариант уезжает ещё и в
     * {@code featData.abilityScoreIncrease}, а полный список — только сюда.
     */
    private List<AbilityBonus> abilityBonuses;
    /**
     * Владение инструментами, выданное без выбора. Навыки, оружие и доспехи потребитель
     * применяет сам и берёт их из {@code featData}; инструменты остаются здесь: словарь
     * инструментов сайта и справочник листа расходятся, и применить их нечем.
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
     * Владение инструментами, выданное без выбора — ссылками на записи справочника.
     *
     * @param tools инструменты
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProficiencyGrant(List<VttgEntityRef> tools) {
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
     *                                   {@code ability}, {@code savingThrow}, {@code option}
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
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Choice(String key, String type, String label, Integer count,
                         Boolean countEqualsProficiencyBonus, List<Option> options,
                         SpellFilter spellFilter, Boolean onlyIfNotProficient,
                         Boolean onlyIfProficient, Boolean expertiseIfProficient,
                         String grants, Boolean rechooseOnLongRest) {
    }

    /**
     * Допустимое значение выбора.
     *
     * @param value значение в словаре своего типа: слаг навыка, ключ типа урона, url записи
     * @param name  подпись для игрока
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Option(String value, String name) {
    }

    /**
     * Чем ограничен выбор заклинания.
     *
     * @param level       точный уровень; 0 — заговор
     * @param maxLevel    наибольший допустимый уровень
     * @param schools     школы магии
     * @param classes     классы, из списков которых можно выбирать
     * @param castingTime время накладывания ({@code ritual}, {@code action}, …)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpellFilter(Integer level, Integer maxLevel, List<String> schools,
                              List<VttgEntityRef> classes, String castingTime) {
    }
}
