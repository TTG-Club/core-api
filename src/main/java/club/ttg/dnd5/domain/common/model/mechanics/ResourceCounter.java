package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ресурс со счётчиком: очки удачи «Удачливого», применения «Целителя», заряды
 * «Ритуального заклинателя».
 *
 * <p>От классового ресурса отличается тем, что у него нет ни уровня начала, ни прогрессии
 * по уровням: черта либо взята, либо нет. Поэтому и модель проще табличной колонки класса
 * ({@code ClassTableColumn}) — ключ, подпись, максимум и откат.</p>
 *
 * <p>Максимум задан ФОРМУЛОЙ, а не числом: у большинства таких ресурсов он привязан к
 * бонусу мастерства («Удачливый» — очки удачи по бонусу мастерства) и обязан расти вместе
 * с ним. Числовой максимум записывается той же формулой — просто числом.</p>
 *
 * <p>Ограничение «один раз до продолжительного отдыха» у выданного заклинания — это тоже
 * ресурс и описывается здесь же: у {@link SpellGrant} своих счётчиков нет.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceCounter {
    /**
     * Стабильный ключ ресурса в пределах черты: по нему лист хранит потраченный остаток и
     * переживает правку названия.
     */
    @Schema(description = "Стабильный ключ ресурса в пределах черты", example = "luck-points")
    private String key;

    @Schema(description = "Название на листе", example = "Очки удачи")
    private String name;

    /**
     * Краткое название для компактной плитки счётчика. Пусто — плитка подписывается
     * полным названием.
     */
    @Schema(description = "Краткое название для компактной плитки", example = "Удача")
    private String shortName;

    /**
     * Формула максимума: число либо выражение с {@code @prof} (бонус мастерства),
     * {@code @level} (суммарный уровень) и {@code @mod.<abbr>} (модификатор
     * характеристики — {@code @mod.wis}).
     *
     * <p>Строкой, а не разобранным выражением: грамматика формулы — это контракт листа,
     * и второй её разбор здесь означал бы второй диалект, который разошёлся бы с первым.
     * Кривая формула лист не роняет — она читается как ноль.</p>
     */
    @Schema(description = "Формула максимума: число либо выражение с @prof, @level, @mod.<abbr>",
            example = "@prof")
    private String max;

    @Schema(description = "Каким отдыхом восстанавливается",
            examples = {"SHORT_REST", "LONG_REST"})
    private ResourceRecovery recovery;

    /**
     * Откат с поправкой на записи, где поле не заполнено: продолжительный отдых — общий
     * случай, короткий проставляют явно.
     *
     * @return каким отдыхом восстанавливается ресурс.
     */
    public ResourceRecovery resolveRecovery() {
        return recovery == null ? ResourceRecovery.LONG_REST : recovery;
    }
}
