package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Повышение характеристик, которое даёт умение класса или черта.
 *
 * <p>Описывает не готовую прибавку, а выбор игрока: из {@link #abilities} он берёт
 * {@link #count} характеристик и повышает каждую на {@link #bonus}, но не выше {@link #upto}.
 * Поэтому одна сущность может предлагать несколько взаимоисключающих вариантов: «Улучшение
 * характеристик» — это {@code +2 к одной} и {@code +1 к двум} на выбор, а эпические дары
 * отличаются от обычных черт только пределом ({@code 30} против {@code 20}).</p>
 *
 * <p>Постоянные прибавки, которые игрок не выбирает, описывает
 * {@link ActiveEffect} — здесь только выбор.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbilityBonus {
    @Schema(description = "Характеристики, из которых игрок выбирает",
            examples = {"STRENGTH", "DEXTERITY"})
    private List<Ability> abilities;

    @Schema(description = "На сколько повышается каждая выбранная характеристика", example = "1")
    private Integer bonus;

    @Schema(description = "Предел, выше которого характеристику не поднять", example = "20")
    private Integer upto;

    @Schema(description = "Сколько характеристик выбирают из списка; null — одну", example = "1")
    private Integer count;

    /**
     * Ключ выбора, который игрок уже сделал: характеристика берётся оттуда, а не
     * спрашивается второй раз. Так устроен «Устойчивый» — там повышают ту самую
     * характеристику, спасбросками которой овладели.
     */
    @Schema(description = "Ключ ранее сделанного выбора, к которому привязано повышение",
            example = "saving-throw")
    private String fromChoiceKey;

    /**
     * Количество выбираемых характеристик с поправкой на записи, сохранённые до появления
     * поля: у них {@code count} пуст и означает одну характеристику.
     */
    public int resolveCount() {
        return count == null || count < 1 ? 1 : count;
    }
}
