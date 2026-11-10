package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Что возвращает ресурсу один вид отдыха: ничего, все заряды или своё число.
 *
 * <p>Короткий и продолжительный отдых описываются каждый своим правилом — так же, как
 * ресурс в листе персонажа. Одним словом отката ({@link ResourceRecovery}) не выразить,
 * например, «два заряда на коротком, все на продолжительном» или ресурс, который
 * продолжительный отдых возвращает лишь частично.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CounterRestRule {
    /** Наименьшее число зарядов, которое возвращает отдых в режиме «своё число». */
    public static final int MIN_AMOUNT = 1;

    @Schema(description = "Сколько возвращает отдых", example = "ALL")
    private CounterRestMode mode;

    @Schema(description = "Число возвращаемых зарядов; учитывается только при режиме AMOUNT",
            example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer amount;

    /**
     * Режим с поправкой на незаполненное поле: правило без режима ничего не возвращает.
     *
     * @return режим восстановления.
     */
    public CounterRestMode resolveMode() {
        return mode == null ? CounterRestMode.NONE : mode;
    }

    /**
     * Число зарядов с поправкой на пустое и слишком малое значение.
     *
     * @return число возвращаемых зарядов, не меньше {@link #MIN_AMOUNT}.
     */
    public int resolveAmount() {
        return amount == null || amount < MIN_AMOUNT ? MIN_AMOUNT : amount;
    }

    /** @return правило «отдых ничего не возвращает». */
    public static CounterRestRule none() {
        return new CounterRestRule(CounterRestMode.NONE, null);
    }

    /** @return правило «отдых возвращает все заряды». */
    public static CounterRestRule all() {
        return new CounterRestRule(CounterRestMode.ALL, null);
    }

    /**
     * @param amount число возвращаемых зарядов.
     * @return правило «отдых возвращает своё число».
     */
    public static CounterRestRule amount(int amount) {
        return new CounterRestRule(CounterRestMode.AMOUNT, amount);
    }
}
