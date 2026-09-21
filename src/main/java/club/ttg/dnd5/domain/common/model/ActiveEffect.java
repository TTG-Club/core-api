package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Активный эффект, совместимый с системой Active Effects VTTG. Одна и та же
 * модель у всего, что меняет числа на листе персонажа: заклинаний, магических
 * предметов и черт.
 * <p>
 * Хранится как JSONB и передаётся в VTTG без преобразования словарей, поэтому
 * значения (характеристики, режимы, ключи состояний, флаги) держим строками в
 * вокабуляре VTTG, а не доменными enum'ами core-api. Ключ изменения
 * ({@link Change#key}) — это {@code EffectTargetKey} VTTG: {@code armorClass},
 * {@code save.constitution}, {@code skill.stealth}, {@code movement.swim},
 * {@code ability.strength}, {@code attack.melee} и прочие.
 * <p>
 * Ключи, которых модель ещё не знает, у эффекта и у {@link Save}, {@link Change},
 * {@link Aura} не выбрасываются, а хранятся и выгружаются как есть — см.
 * {@link UnknownFieldsHolder}.
 *
 * @see club.ttg.dnd5.domain.vttg
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveEffect extends UnknownFieldsHolder {
    private String id;
    private String name;
    private String description;
    private String icon;
    private Boolean disabled;
    private String origin;
    private String originId;
    private Boolean transfer;
    private Duration duration;
    private List<Change> changes;
    private List<String> flags;
    private Aura aura;
    private String areaTrigger;
    private String effectTarget;
    private String conditionKey;
    /** Степень Истощения 1–6; имеет смысл только при {@code conditionKey = exhaustion}. */
    private Integer exhaustionLevel;
    /**
     * Условие наложения: эффект ложится, только если оно выполнено. Строка словаря
     * срабатываний на событии «при наложении» —
     * {@code source.weaponMastery === true} у приёма оружия «Опрокидывание».
     */
    private String landingCondition;
    /**
     * Условие броска: эффект не входит в числа листа и работает только в бросках,
     * где условие выполнено. Строка словаря модификаторов —
     * {@code target.allyAdjacent} у «Тактики стаи».
     */
    private String rollCondition;
    /** Вариант: из эффектов одной группы ложится один («Глухота/слепота»). */
    private Variant variant;
    /** Применение или включение: без него эффект действует постоянно. */
    private Activation activation;
    /** Заряды срабатываний: каждое сработавшее тратит один («Огненный щит»). */
    private Charges charges;
    /**
     * Формула, которая бросается один раз при наложении и подставляется вместо
     * {@code @roll} во все формулы эффекта («Вибрирующие жидкости»).
     */
    private String savedRoll;
    /** Срок формулой («1к4» раунда) вместо числа {@link Duration#value}. */
    private String durationFormula;
    private Save applySave;
    private Boolean applyOnSuccess;
    private Boolean applyOnSuccessOnly;
    /** Триггер потребления эффекта (например {@code carrierAttack} у «Злой насмешки»). */
    private String consumeOn;
    private List<DamagePart> damageParts;
    private RecurringSave recurringSave;
    private RecurringDamage recurringDamage;
    /** Ключи состояний, которые эффект подавляет, не снимая («Свобода перемещения»). */
    private List<String> suppressConditions;
    private List<String> conditionImmunities;
    /**
     * Срабатывания VTTG: «событие → условие → спасбросок → действия → лимит».
     * Хранятся как есть, без разбора: модель срабатывания развивается на стороне
     * системы (события следующих фаз уже зарезервированы), и бэкенд её не
     * проверяет и не обрезает.
     */
    private List<JsonNode> triggers;
    /** Действие «вырваться» — кнопка на листе («Опутывание»: проверка Атлетики). */
    private Escape escape;
    /** Ступени эффекта («Проклятие гибельного старения»), до 10. */
    private List<Stage> stages;
    /**
     * Действующая ступень, 0–9; форма пишет её сама, а {@link #changes} и
     * {@link #flags} эффекта копирует из этой ступени.
     */
    private Integer stageIndex;

    /** Длительность эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Duration {
        private String type;
        private Integer value;
        private Integer remaining;
        /**
         * Якорь хода для {@code type: "turn"} — чей ход прекращает эффект:
         * {@code carrier} (носитель, по умолчанию) либо {@code source} (кастер).
         */
        private String turnAnchor;
        /**
         * Момент хода якоря для {@code type: "turn"}: {@code start} либо
         * {@code end} (по умолчанию).
         */
        private String turnTiming;
    }

    /** Числовой модификатор (change). */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Change extends UnknownFieldsHolder {
        private String key;
        private String mode;
        private String value;
        private String condition;
        /** Модификатор растёт или убывает со временем; только у плоского числа. */
        private ChangeStep step;
        private Integer priority;
    }

    /** Шаг изменения: на сколько и как часто двигается значение строки. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChangeStep {
        /** На сколько за период; отрицательное — значение убывает. */
        private Integer by;
        /** {@code turn} либо {@code round}. */
        private String per;
        /** Предел, дальше которого значение не уходит; нет — без предела. */
        private Integer until;
    }

    /** Настройки ауры эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Aura extends UnknownFieldsHolder {
        private Integer radius;
        private String target;
        private Boolean applyToSelf;
        private Boolean visible;
        /**
         * Радиус формулой от носителя ({@code 10 + 20 * floor(@classLevel / 18)}).
         * VTTG считает её при сборе аур и кладёт результат в {@link #radius}.
         */
        private String radiusFormula;
        /** Аура гаснет, пока носитель недееспособен («Аура защиты»). */
        private Boolean whileCapable;
    }

    /**
     * Вариант эффекта в группе альтернатив: эффекты с одним {@link #group}
     * взаимоисключающие, ложится один — выбранный бросающим либо случайный.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Variant {
        private String group;
        private String label;
        /** {@code choose} (по умолчанию) либо {@code random}. */
        private String pick;
    }

    /**
     * Применение или включение эффекта: {@code use} — копия ложится, когда
     * источник применяют (зелье, боеприпас, кнопка «Применить»); {@code toggle} —
     * эффект лежит выключенным и включается переключателем («Ярость»).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Activation {
        private String mode;
        /**
         * Ключ счётчика листа, который тратит применение или включение — как
         * {@code VttgClass.Counter.key} ({@code rages}); нет — ничего не тратит.
         */
        private String counter;
        /** Сколько тратится со счётчика; нет — одна единица. */
        private Integer amount;
    }

    /** Заряды эффекта: сколько раз ещё сработают его срабатывания. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Charges {
        private Integer max;
        /** Сколько осталось; автор ставит равным {@link #max}. */
        private Integer current;
        /** Последний заряд снимает эффект; нет — эффект остаётся пустым. */
        private Boolean endsWhenEmpty;
    }

    /** Спасбросок при наложении эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Save extends UnknownFieldsHolder {
        private String ability;
        private Integer dc;
        private String onSuccess;
        /** Согласная цель не бросает спасбросок. */
        private Boolean allowWilling;
    }

    /** Периодический спасбросок для снятия эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecurringSave {
        private String ability;
        private Integer dc;
        private String timing;
    }

    /** Периодический урон (DoT). */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecurringDamage {
        private List<DamagePart> damageParts;
        private String timing;
        /**
         * Спасбросок против урона на каждом тике: провал — полный урон, успех — без
         * урона ({@code negate}) или половина ({@code half}). Эффект при этом
         * остаётся — снимает его только {@link RecurringSave}. {@code dc = 0} — Сл
         * наложившего, её проставляет VTTG при наложении.
         */
        private Save save;
    }

    /** Действие, снимающее эффект: «проверка Силы (Атлетика) Сл 14 — и вырваться». */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Escape {
        /** {@code self} либо {@code adjacent}; нет — сам носитель. */
        private String by;
        /** {@code action}, {@code bonus}, {@code reaction}, {@code move} либо {@code free}. */
        private String cost;
        /** Футы перемещения при {@code cost = move}. */
        private Integer moveCostFeet;
        /** Проверка навыка; нет — снимает без броска. */
        private EscapeCheck check;
        /** {@code removeSelf} либо {@code removeCondition}; нет — снимается сам эффект. */
        private String onSuccess;
        /** Подпись кнопки; нет — «Вырваться». */
        private String label;
    }

    /** Проверка навыка, снимающая эффект. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EscapeCheck {
        /** Ключ навыка системы ({@code athletics}). */
        private String skill;
        /** Сложность; 0 — Сл источника. */
        private Integer dc;
    }

    /** Ступень эффекта: свой набор модификаторов и флагов. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Stage {
        private String label;
        private List<Change> changes;
        private List<String> flags;
    }

}
