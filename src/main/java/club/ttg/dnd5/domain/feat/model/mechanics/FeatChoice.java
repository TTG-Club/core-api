package club.ttg.dnd5.domain.feat.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Выбор, который игрок делает <b>в момент взятия черты</b>: навык, тип урона, заклинание,
 * заклинательная характеристика.
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
public class FeatChoice {
    /**
     * Стабильный ключ в пределах черты: на него ссылается {@code AbilityBonus.fromChoiceKey},
     * по нему лист хранит сделанный выбор и переживает правку описания.
     */
    @Schema(description = "Стабильный ключ выбора в пределах черты", example = "damage-type")
    private String key;

    @Schema(description = "Что выбирают", example = "DAMAGE_TYPE")
    private ChoiceType type;

    @Schema(description = "Подпись для игрока", example = "Выберите тип урона")
    private String label;

    @Schema(description = "Сколько значений выбирают; null — одно", example = "1")
    private Integer count;

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

    /** Количество с поправкой на записи, где поле не заполнено: это один выбор. */
    public int resolveCount() {
        return count == null || count < 1 ? 1 : count;
    }
}
