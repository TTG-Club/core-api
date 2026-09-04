package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.EntityRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Выдача ВСЕГО списка заклинаний класса: «персонаж знает все заклинания друида».
 *
 * <p>Ссылками на конкретные заклинания ({@link SpellGrant#getSpells()}) такое не
 * описывается: список пришлось бы дописывать руками при каждом пополнении справочника, а
 * персонаж, созданный вчера, отличался бы от созданного сегодня не по правилам, а по
 * тому, когда автор дошёл до записи. Здесь хранится не перечень, а правило — список
 * разворачивается в момент выдачи, поэтому новое заклинание класса достаётся новым
 * персонажам само.</p>
 *
 * <p>Групп бывает несколько, и это НЕ взаимоисключающие варианты: каждая открывается на
 * своём {@link #getRequiredLevel()} и складывается с предыдущими — ровно как ступени
 * {@link SpellListGroup} у расширения списка.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassSpellListGrant {
    /**
     * Уровень персонажа, с которого группа выдаётся. {@code null} — с момента взятия
     * записи.
     */
    @Schema(description = "Уровень персонажа, с которого выдаётся список", example = "10")
    private Integer requiredLevel;

    /**
     * Классы, чьи списки заклинаний выдаются. Несколько — это объединение списков, а не
     * выбор: выбор списка описывается выбором игрока ({@link ChoiceType#SPELL_LIST}), а
     * здесь выдача без выбора.
     */
    @Schema(description = "Классы, чьи списки заклинаний выдаются")
    private List<EntityRef> classes;

    /**
     * Ровно этот круг: «все заговоры друида». {@code null} — круг сверху не задан.
     *
     * <p>Именами полей повторяет {@link SpellFilter}: там круг задан теми же двумя полями,
     * и второй способ описать одно и то же читался бы как другой смысл.</p>
     */
    @Schema(description = "Ровно этот круг заклинаний", example = "0")
    private Integer level;

    /** Не выше этого круга. {@code null} — верхней границы нет. */
    @Schema(description = "Не выше этого круга заклинаний", example = "3")
    private Integer maxLevel;

    /**
     * Круг берётся из ячеек заклинаний персонажа и растёт вместе с ним: друид знает свой
     * список ровно до того круга, который способен наложить.
     *
     * <p>Сервер такую границу поставить не может — персонажа он не знает, — поэтому
     * развёрнутые заклинания уезжают потребителю целиком, помеченные
     * {@code GrantedSpellResponse.limitedBySlots}, а круг режет уже лист персонажа. Взведён
     * — {@link #getLevel()} и {@link #getMaxLevel()} не заполняются: три границы разом
     * означали бы три разных ответа на один вопрос.</p>
     */
    @Schema(description = "Круг ограничен доступными персонажу ячейками заклинаний")
    private Boolean maxLevelFromSlots;

    /**
     * Характеристика, от которой считаются ЭТИ заклинания. {@code null} — группа её не
     * задаёт, и характеристику берут выше: сперва ответ игрока, затем
     * {@link SpellGrant#getSpellcastingAbility()}, затем класс, чья это магия.
     *
     * <p>У группы, а не только у записи целиком: один набор заклинаний записи может
     * считаться от одной характеристики, другой — от другой, и одно поле на всех
     * заставило бы заводить ради этого вторую запись.</p>
     */
    @Schema(description = "Характеристика заклинаний группы", examples = {"WISDOM", "CHARISMA"})
    private Ability spellcastingAbility;

    /**
     * ЭТИ заклинания не нужно готовить. {@code null} — группа отметку не задаёт, и её
     * берут у записи ({@link SpellGrant#getAlwaysPrepared()}).
     *
     * <p>У группы по той же причине, что и характеристику: заклинания домена всегда
     * подготовлены, а заклинание, выданное тем же умением сверх них, подготовку
     * занимает.</p>
     */
    @Schema(description = "Заклинания группы всегда подготовлены")
    private Boolean alwaysPrepared;
}
