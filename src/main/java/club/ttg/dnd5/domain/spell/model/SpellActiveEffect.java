package club.ttg.dnd5.domain.spell.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Активный эффект заклинания, совместимый с системой Active Effects VTTG.
 * <p>
 * Хранится как JSONB и передаётся в VTTG без преобразования словарей, поэтому
 * значения (характеристики, режимы, ключи состояний, флаги) держим строками в
 * вокабуляре VTTG, а не доменными enum'ами core-api.
 *
 * @see club.ttg.dnd5.domain.vttg
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellActiveEffect {
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
    private Save applySave;
    private Boolean applyOnSuccess;
    private Boolean applyOnSuccessOnly;
    /** Триггер потребления эффекта (например {@code carrierAttack} у «Злой насмешки»). */
    private String consumeOn;
    private List<DamagePart> damageParts;
    private RecurringSave recurringSave;
    private RecurringDamage recurringDamage;
    private List<String> conditionImmunities;

    /** Длительность эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Duration {
        private String type;
        private Integer value;
        private Integer remaining;
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
    }

    /** Часть урона/лечения эффекта. */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DamagePart {
        private String formula;
        private String type;
        private String target;
        private Boolean requiresDamage;
    }
}
