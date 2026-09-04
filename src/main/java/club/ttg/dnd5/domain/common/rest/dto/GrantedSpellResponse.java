package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Выдаваемое записью справочника заклинание: запись каталога плюс уровень, с которого оно
 * доступно.
 *
 * <p>Форма повторяет {@code SpeciesInnateSpellResponse}: у вида врождённые заклинания
 * отдаются ровно так же. Одна форма на всех, кто выдаёт заклинания — черта, предыстория,
 * умение класса, — потому что сайт рисует их одной таблицей.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantedSpellResponse {
    @Schema(description = "Заклинание с данными справочника")
    private SpellShortResponse spell;

    /**
     * Уровень персонажа, с которого заклинание доступно. {@code null} — с момента взятия
     * черты.
     */
    @Schema(description = "Уровень персонажа, с которого доступно заклинание", example = "3")
    private Integer requiredLevel;

    /**
     * Круг заклинания ограничен ячейками персонажа: заклинание выдано группой «весь список
     * класса» с отметкой {@code maxLevelFromSlots}.
     *
     * <p>{@code null} — ограничения нет. Сервер здесь ничего отфильтровать не может: он не
     * знает ни классов персонажа, ни его уровня, — поэтому список уезжает целиком, а круг
     * режет лист, у которого ячейки посчитаны.</p>
     */
    @Schema(description = "Круг ограничен доступными персонажу ячейками заклинаний")
    private Boolean limitedBySlots;

    /**
     * Характеристика, от которой считается ЭТО заклинание. {@code null} — группа её не
     * задаёт: характеристику подставит потребитель — ответом игрока, характеристикой
     * записи либо характеристикой класса.
     *
     * <p>Именно {@code null}, а не подставленная запасная: ответ игрока на выбор
     * характеристики старше записи, а сервер ответа не знает. Подставь сервер запасную
     * — выбор игрока перестал бы что-либо менять.</p>
     */
    @Schema(description = "Характеристика заклинаний группы", examples = {"WISDOM", "CHARISMA"})
    private Ability spellcastingAbility;

    /**
     * ЭТО заклинание не нужно готовить. {@code null} — группа отметку не задаёт, и
     * потребитель берёт её у записи.
     */
    @Schema(description = "Заклинание группы всегда подготовлено")
    private Boolean alwaysPrepared;

    public GrantedSpellResponse(SpellShortResponse spell, Integer requiredLevel) {
        this(spell, requiredLevel, null, null, null);
    }

    public GrantedSpellResponse(SpellShortResponse spell, Integer requiredLevel, Boolean limitedBySlots) {
        this(spell, requiredLevel, limitedBySlots, null, null);
    }
}
