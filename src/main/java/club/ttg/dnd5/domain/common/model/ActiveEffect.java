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
 *
 * @see club.ttg.dnd5.domain.vttg
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveEffect {
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
    private Save applySave;
    private Boolean applyOnSuccess;
    private Boolean applyOnSuccessOnly;
    /** Триггер потребления эффекта (например {@code carrierAttack} у «Злой насмешки»). */
    private String consumeOn;
    private List<DamagePart> damageParts;
    private RecurringSave recurringSave;
    private RecurringDamage recurringDamage;
    private List<String> conditionImmunities;
    /**
     * Срабатывания VTTG: «событие → условие → спасбросок → действия → лимит».
     * Хранятся как есть, без разбора: модель срабатывания развивается на стороне
     * системы (события следующих фаз уже зарезервированы), и бэкенд её не
     * проверяет и не обрезает.
     */
    private List<JsonNode> triggers;

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
    public static class Change {
        private String key;
        private String mode;
        private String value;
        private String condition;
        private Integer priority;
    }

    /** Настройки ауры эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Aura {
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

    /** Спасбросок при наложении эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Save {
        private String ability;
        private Integer dc;
        private String onSuccess;
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

}
