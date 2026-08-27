package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Выбор, который игрок делает <b>один раз, получая источник эффекта</b>: беря черту или
 * выбирая вид. Навык, тип урона, заклинание, заклинательная характеристика.
 *
 * <p>Выборы по ходу игры («выберите существо в пределах 30 футов», «оттолкнуть или сбить
 * с ног — на ваш выбор») сюда не попадают: лист их не запоминает, они остаются в описании.</p>
 *
 * <p>Повышение характеристик — тоже выбор, но у него своя модель
 * ({@code mechanics.abilityBonuses}), и дублировать его здесь не нужно. Если повышение
 * применяется к уже выбранному значению, как у «Устойчивого», выбор описывается здесь,
 * а бонус ссылается на него через {@code AbilityBonus.fromChoiceKey}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MechanicChoice {
    /**
     * Стабильный ключ в пределах черты: на него ссылается {@code AbilityBonus.fromChoiceKey},
     * по нему лист хранит сделанный выбор и переживает правку описания.
     */
    @Schema(description = "Стабильный ключ выбора в пределах черты", example = "damage-type")
    private String key;

    /**
     * Основной вид выбора. Когда вид один — он здесь и есть; когда их несколько, здесь
     * первый, а полный набор в {@link #types}. Так запись остаётся читаемой для тех, кто
     * про смешанные наборы не знает.
     */
    @Schema(description = "Что выбирают (основной вид)", example = "DAMAGE_TYPE")
    private ChoiceType type;

    /**
     * Полный набор видов, когда выбирают из нескольких справочников сразу: «Умелый» берёт
     * три штуки вперемешку из навыков и инструментов.
     *
     * <p>Заполняется только при нескольких видах — у обычного выбора хватает {@link #type}.
     * Куда лечь выбранному значению, решает принадлежность самого значения:
     * {@code sleightOfHand} есть среди навыков, {@code thieves-tools} — среди
     * инструментов.</p>
     *
     * <p>Поэтому смешивать можно ТОЛЬКО виды со справочником правил (см. {@link ChoiceType}).
     * Оружие, приёмы оружия, заклинания и «вариант» описаны данными мира, и по значению их
     * не разложить — набор с ними выгрузка сворачивает до одного вида.</p>
     */
    @Schema(description = "Полный набор видов при выборе из нескольких справочников сразу",
            examples = {"SKILL", "TOOL"})
    private List<ChoiceType> types;

    @Schema(description = "Подпись для игрока", example = "Выберите тип урона")
    private String label;

    @Schema(description = "Сколько значений выбирают; null — одно", example = "1")
    private Integer count;

    /**
     * Уровень персонажа, с которого выбор открывается; {@code null} — сразу.
     *
     * <p>Нужен умению, которое спрашивает одно и то же не один раз: компетентность плут
     * получает на первом уровне и ещё раз на шестом, бард — на втором и на девятом. Второе
     * умение под это не заводят — в книге оно одно, а повтор описан строкой роста, —
     * поэтому у выбора свой уровень.</p>
     */
    @Schema(description = "Уровень персонажа, с которого выбор открывается; null — сразу",
            example = "6")
    private Integer requiredLevel;

    /**
     * Количество равно бонусу мастерства и растёт вместе с ним: «Ритуальный заклинатель»
     * выбирает столько ритуальных заклинаний, каков бонус мастерства. Если задано,
     * {@link #count} не используется.
     */
    @Schema(description = "Количество равно бонусу мастерства")
    private Boolean countEqualsProficiencyBonus;

    /**
     * Допустимые значения. Пусто — подходит любое значение своего типа: «владение 3 любыми
     * навыками или инструментами» у «Одарённого».
     */
    @Schema(description = "Допустимые значения; пусто — любое значение своего типа")
    private List<ChoiceOption> options;

    @Schema(description = "Ограничение выбора заклинания или заговора")
    private SpellFilter spellFilter;

    /**
     * Выбирать можно только то, чем персонаж ещё не владеет: «выберите характеристику,
     * спасброском которой вы не владеете», «выберите навык, в котором у вас нет
     * компетентности».
     */
    @Schema(description = "Только то, чем персонаж ещё не владеет")
    private Boolean onlyIfNotProficient;

    /**
     * Выбирать можно только то, чем персонаж уже владеет: «выберите навык, которым вы
     * владеете» — так устроен «Знаток», поднимающий выбранное до компетентности.
     *
     * <p>Обратен {@link #onlyIfNotProficient}: вместе они бессмысленны и оставляют
     * пустой пул, поэтому форма редактора даёт отметить только один из двух.</p>
     */
    @Schema(description = "Только то, чем персонаж уже владеет")
    private Boolean onlyIfProficient;

    /**
     * Что даёт выбор: владение (по умолчанию) или компетентность. Отдельным полем, а не
     * ещё одним флагом рядом с {@link #expertiseIfProficient}: тот описывает условную
     * замену, а здесь исход безусловен.
     */
    @Schema(description = "Что даёт выбор", examples = {"PROFICIENCY", "EXPERTISE"})
    private ChoiceGrant grants;

    /**
     * Если персонаж уже владеет выбранным, вместо владения он получает компетентность —
     * «Наблюдательный», «Острый ум», «Аберрантная анатомия».
     */
    @Schema(description = "Если владение уже есть — выдать компетентность")
    private Boolean expertiseIfProficient;

    /**
     * Выбор пересматривается на продолжительном отдыхе: «Мастер оружия» меняет тип оружия,
     * «Дар устойчивости к энергиям» — типы урона, «Тактик Жентарима» — навык компетентности.
     */
    @Schema(description = "Выбор можно менять на продолжительном отдыхе")
    private Boolean rechooseOnLongRest;

    /**
     * Виды выбора одним списком: {@link #types}, если набор смешанный, иначе один
     * {@link #type}. Пустой список — у записи не заполнено ни то, ни другое; такой выбор
     * бессмыслен и до потребителя не едет.
     *
     * @return виды в порядке, заданном автором.
     */
    public List<ChoiceType> resolveTypes() {
        if (!CollectionUtils.isEmpty(types)) {
            return types.stream().filter(Objects::nonNull).distinct().toList();
        }
        return type == null ? List.of() : List.of(type);
    }

    /**
     * Основной вид выбора с поправкой на записи, где заполнен только смешанный набор.
     *
     * @return {@link #type}, иначе первый вид из {@link #types}; {@code null} — видов нет.
     */
    public ChoiceType resolveType() {
        if (type != null) {
            return type;
        }
        List<ChoiceType> resolved = resolveTypes();
        return resolved.isEmpty() ? null : resolved.get(0);
    }

    /** Количество с поправкой на записи, где поле не заполнено: это один выбор. */
    public int resolveCount() {
        return count == null || count < 1 ? 1 : count;
    }

    /**
     * Исход выбора с поправкой на записи, где поле не заполнено: до появления
     * компетентности любой выбор давал владение.
     *
     * @return что даёт выбор.
     */
    public ChoiceGrant resolveGrant() {
        return grants == null ? ChoiceGrant.PROFICIENCY : grants;
    }
}
