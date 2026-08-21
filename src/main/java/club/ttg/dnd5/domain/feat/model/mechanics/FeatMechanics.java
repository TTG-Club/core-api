package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
import club.ttg.dnd5.domain.common.model.mechanics.ProficiencyGrant;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SheetModifiers;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Механика влияния черты на лист персонажа — по образцу
 * {@code MagicItemMechanics}: один JSONB-контейнер, который растёт по мере того, как
 * очередной блок эффектов черт переезжает из описания в данные.
 *
 * <p>Блоки заводятся по одному, по мере того как очередной кусок эффектов черты переезжает
 * из описания в данные: повышение характеристик, выборы при взятии, постоянные модификаторы
 * листа, владения, заклинания, ресурсы. Условные эффекты («Оборона» — +1 к КД только в
 * доспехе) сюда не идут и остаются в описании: условие пришлось бы описывать отдельным
 * языком, а лист такие эффекты всё равно показывает справкой.</p>
 *
 * <p><b>Формат — контракт с сайтом.</b> Ответ детальника и тело запроса мастерской отдают
 * эту модель как есть (см. {@code FeatDetailResponse.mechanics},
 * {@code FeatRequest.mechanics}), поэтому имена полей и значения enum'ов менять нельзя
 * молча: это ломает и редактор, и уже сохранённый JSONB. Выгрузка в VTTG свою форму строит
 * отдельно ({@code VttgFeatMechanicsMapper}) — правки здесь до неё не доезжают сами.</p>
 *
 * <p>{@code null} — у черт, сохранённых до появления поля, и у тех, чьё действие
 * описано только текстом.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatMechanics {
    /**
     * Варианты повышения характеристик. Несколько элементов — это выбор «или/или»:
     * «Улучшение характеристик» даёт {@code +2 к одной} либо {@code +1 к двум}.
     */
    @Schema(description = "Варианты повышения характеристик (несколько элементов — выбор «или»)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<AbilityBonus> abilityBonuses;

    /**
     * Выборы, которые игрок делает при взятии черты. Повышение характеристик сюда не
     * дублируется — оно живёт в {@link #abilityBonuses}.
     */
    @Schema(description = "Выборы, которые игрок делает при взятии черты",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MechanicChoice> choices;

    /**
     * Постоянные модификаторы листа: хиты, скорости, КД, чувства, защиты. Сгруппированы
     * отдельно, чтобы механика не превратилась в плоский мешок из тридцати полей, когда
     * подтянутся ресурсы.
     */
    @Schema(description = "Постоянные модификаторы листа персонажа",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SheetModifiers modifiers;

    /**
     * Владения, выданные чертой без выбора. Отдельным блоком от {@link #modifiers}:
     * тот описывает числа шапки и блока защит, а владения — свой раздел листа со своим
     * справочником категорий. Выбираемые владения живут в {@link #choices}.
     */
    @Schema(description = "Владения, которые черта выдаёт без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ProficiencyGrant proficiencies;

    /**
     * Заклинания, которые черта даёт знать без выбора. Своим блоком по той же причине,
     * что и владения: у заклинаний свой раздел листа — книга заклинаний — со своей
     * подготовкой и заклинательной характеристикой. Выбираемые заклинания живут в
     * {@link #choices} вместе со своим фильтром.
     */
    @Schema(description = "Заклинания, которые черта даёт знать без выбора",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SpellGrant spells;

    /**
     * Заклинания, которые черта добавляет в список заклинаний класса — таблица
     * «Заклинания метки» у черт метки дракона.
     *
     * <p>Отдельно от {@link #spells}, потому что это не выдача: такое заклинание игрок не
     * знает, а только может подготовить наравне с классовыми, потратив на него подготовку
     * и ячейку. См. {@link SpellListExpansion}.</p>
     */
    @Schema(description = "Заклинания, добавляемые в список заклинаний класса",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SpellListExpansion spellList;

    /**
     * Ресурсы черты со счётчиком: очки удачи «Удачливого», применения «Целителя».
     *
     * <p>Отдельным блоком от {@link #modifiers}: тот описывает постоянные числа шапки, а
     * ресурс тратится и восстанавливается — у него свой раздел листа со своим откатом от
     * отдыха. Ограничение «один раз до продолжительного отдыха» у выданного заклинания —
     * это тоже ресурс и описывается здесь, а не в {@link #spells}.</p>
     */
    @Schema(description = "Ресурсы черты со счётчиком",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ResourceCounter> counters;
}
