package club.ttg.dnd5.domain.common.model.mechanics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Защита от типа урона, который называет игрок.
 *
 * <p>Сам тип известен только после выбора, поэтому в наборы {@link DamageAffinity} такая
 * защита лечь не может: здесь лежит ссылка на выбор из {@code mechanics.choices} и исход,
 * который выбор даёт. «Закалённая кожа» просит выбрать дробящий или рубящий и даёт к
 * названному сопротивление, «Дар устойчивости к энергиям» — два типа сразу.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamageDefenseFromChoice {
    @Schema(description = "Ключ выбора типа урона из mechanics.choices", example = "damage-type")
    private String choiceKey;

    @Schema(description = "Что выбранный тип урона получает")
    private DamageDefenseKind kind;
}
