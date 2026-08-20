package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Один список заклинаний, которые черта добавляет в список класса, — со своим уровнем
 * доступа и своим количеством.
 *
 * <p>Списков у черты может быть несколько, и это НЕ взаимоисключающие варианты: каждый
 * открывается на своём уровне и складывается с предыдущими. Плоский список
 * ({@link SpellListExpansion#getSpells()}) описать такое не мог — он говорит «вот все
 * заклинания сразу», и черта со ступенчатой таблицей оказывалась сильнее книжной уже на
 * первом уровне.</p>
 *
 * <p>Круг заклинания здесь не хранится — он свойство самой записи справочника и в снимке
 * разошёлся бы с каталогом при первой же правке заклинания. Потребитель берёт круг из
 * записи; см. {@link SpellListExpansion}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellListGroup {
    /**
     * Уровень персонажа, с которого список открывается. {@code null} — с момента взятия
     * черты.
     */
    @Schema(description = "Уровень персонажа, с которого список открывается", example = "5")
    private Integer requiredLevel;

    /**
     * Сколько заклинаний из списка игрок берёт. Пусто — весь список целиком.
     *
     * <p>Формулой, а не числом: у части черт количество привязано к бонусу мастерства или
     * к модификатору характеристики и растёт вместе с персонажем. Грамматика ТА ЖЕ, что у
     * {@link ResourceCounter#getMax()} и у активных эффектов: число либо выражение с
     * {@code @prof}, {@code @level} и {@code @mod.<abbr>}, где {@code abbr} — одно из
     * {@code str|dex|con|int|wis|cha}. Второй диалект той же формулы завести нельзя: лист
     * разбирает её одним разбором, и расхождение здесь он прочитает как ноль.</p>
     */
    @Schema(description = "Сколько заклинаний берут: число либо выражение с @prof, @level, @mod.<abbr>;"
            + " пусто — весь список", example = "@prof")
    private String count;

    /** Заклинания списка. Круг берётся из записи справочника и здесь не хранится. */
    @Schema(description = "Заклинания списка")
    private List<EntityRef> spells;
}
