package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.model.AbilityBonus;
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
 * <p>Сейчас здесь только повышение характеристик — единственное, что лист может
 * применить сам. Остальное (владения, скорость, чувства, заклинания, ресурсы) пока
 * живёт в описании и добавляется сюда отдельными полями.</p>
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
    private List<FeatChoice> choices;

    /**
     * Постоянные модификаторы листа: хиты, скорости, КД, чувства, защиты. Сгруппированы
     * отдельно, чтобы механика не превратилась в плоский мешок из тридцати полей, когда
     * подтянутся ресурсы.
     */
    @Schema(description = "Постоянные модификаторы листа персонажа",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private FeatModifiers modifiers;

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
}
