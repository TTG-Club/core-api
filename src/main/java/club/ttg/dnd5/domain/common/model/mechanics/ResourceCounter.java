package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
     * Сколько зарядов возвращает короткий отдых у отката {@link ResourceRecovery#SHORT_REST_ONE}.
     */
    private static final int SHORT_REST_ONE_AMOUNT = 1;

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
     * {@code @level} (суммарный уровень), {@code @classLevel} (уровень в своём классе) и
     * {@code @mod.<abbr>} (модификатор характеристики — {@code @mod.wis}).
     *
     * <p>Ресурсу класса нужен {@code @classLevel}, а не {@code @level}: у мультиклассового
     * персонажа суммарный уровень больше уровня в классе, и очки чародейства чародея 3 /
     * плута 3 посчитались бы как шесть. Там, где класса-владельца нет (черта, вид,
     * предмет), {@code @classLevel} читается как {@code @level}.</p>
     *
     * <p>Строкой, а не разобранным выражением: грамматика формулы — это контракт листа,
     * и второй её разбор здесь означал бы второй диалект, который разошёлся бы с первым.
     * Кривая формула лист не роняет — она читается как ноль.</p>
     */
    @Schema(description = "Формула максимума: число либо выражение с @prof, @level,"
            + " @classLevel, @mod.<abbr>", example = "@prof")
    private String max;

    /**
     * Ступени максимума по уровням; пусто — максимум задан формулой.
     *
     * <p>Нужны ресурсу, у которого ряд значений формулой не пишется: костей превосходства
     * мастера боевых искусств четыре с третьего уровня, пять с седьмого и шесть с
     * пятнадцатого. Заполнены обе формы — старшей считается ступень: она точнее.</p>
     */
    @Schema(description = "Ступени максимума по уровням; пусто — максимум задан формулой",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CounterScaling> scaling;

    /**
     * Нижняя граница максимума: сколько зарядов у ресурса есть в любом случае.
     *
     * <p>Нужна ресурсам, чей максимум считается модификатором характеристики: вдохновение
     * барда равно модификатору Харизмы, но не меньше одного, и с Харизмой +0 бард всё
     * равно вдохновляет один раз. Минимум не складывается с формулой, а подпирает её
     * снизу. Пусто — нижней границы нет.</p>
     */
    @Schema(description = "Нижняя граница максимума: ниже неё формула максимум не опускает",
            example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer min;

    /**
     * Показывать ресурс колонкой таблицы прогрессии.
     *
     * <p>Ряд по уровням у ресурса уже задан — ступенями либо формулой, — и колонка книги
     * собирается из него ({@code CounterTableColumns}): второй раз те же числа автор не
     * набирает. Колонка выводится, только если ряд считается от одного уровня; максимум
     * по модификатору характеристики одинакового ряда для всех не имеет.</p>
     */
    @Schema(description = "Показывать ресурс колонкой таблицы прогрессии")
    private boolean showInTable;

    /**
     * Откат одним словом. Читается, только если раздельных правил {@link #shortRest} и
     * {@link #longRest} нет — у записей, сохранённых до них.
     */
    @Schema(description = "Каким отдыхом восстанавливается (легаси: читается, если нет"
            + " shortRest и longRest)",
            examples = {"SHORT_REST", "LONG_REST", "SHORT_REST_ONE"})
    private ResourceRecovery recovery;

    /**
     * Что возвращает короткий отдых. Вместе с {@link #longRest} описывает восстановление
     * раздельно, как ресурс листа: «один заряд коротким, все продолжительным», «два
     * заряда коротким», «ничего коротким, три заряда продолжительным».
     */
    @Schema(description = "Что возвращает короткий отдых; нет обоих правил — выводится из recovery",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CounterRestRule shortRest;

    /** Что возвращает продолжительный отдых. */
    @Schema(description = "Что возвращает продолжительный отдых; нет обоих правил — выводится"
            + " из recovery", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CounterRestRule longRest;

    /**
     * Нижняя граница максимума с поправкой на её отсутствие: ноль и отрицательное число
     * границей не являются — ресурса меньше чем на ноль зарядов не бывает.
     *
     * @return нижняя граница максимума; {@code null} — её нет.
     */
    public Integer resolveMin() {
        return min == null || min <= 0 ? null : min;
    }

    /**
     * Откат с поправкой на записи, где поле не заполнено: продолжительный отдых — общий
     * случай, короткий проставляют явно.
     *
     * @return каким отдыхом восстанавливается ресурс.
     */
    public ResourceRecovery resolveRecovery() {
        if (hasRestRules()) {
            // Ближайшее одно слово для потребителей, которые правил ещё не читают:
            // различаются три значения только коротким отдыхом
            return switch (resolveShortRest().resolveMode()) {
                case ALL -> ResourceRecovery.SHORT_REST;
                case AMOUNT -> ResourceRecovery.SHORT_REST_ONE;
                case NONE -> ResourceRecovery.LONG_REST;
            };
        }
        return recovery == null ? ResourceRecovery.LONG_REST : recovery;
    }

    /**
     * Что возвращает короткий отдых. Есть хоть одно раздельное правило — недостающее
     * читается как «ничего»; нет ни одного — правило выводится из отката одним словом.
     *
     * @return правило короткого отдыха.
     */
    public CounterRestRule resolveShortRest() {
        if (hasRestRules()) {
            return shortRest == null ? CounterRestRule.none() : shortRest;
        }
        ResourceRecovery legacy = recovery == null ? ResourceRecovery.LONG_REST : recovery;
        return switch (legacy) {
            case SHORT_REST -> CounterRestRule.all();
            case SHORT_REST_ONE -> CounterRestRule.amount(SHORT_REST_ONE_AMOUNT);
            case LONG_REST -> CounterRestRule.none();
        };
    }

    /**
     * Что возвращает продолжительный отдых. Без раздельных правил — все заряды: короткий
     * отдых в правилах короче продолжительного, и ресурс, который вернул короткий,
     * возвращает и продолжительный.
     *
     * @return правило продолжительного отдыха.
     */
    public CounterRestRule resolveLongRest() {
        if (hasRestRules()) {
            return longRest == null ? CounterRestRule.none() : longRest;
        }
        return CounterRestRule.all();
    }

    private boolean hasRestRules() {
        return shortRest != null || longRest != null;
    }
}
