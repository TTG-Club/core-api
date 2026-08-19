package club.ttg.dnd5.domain.common.model.mechanics;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Отношение персонажа к типам урона — то, что лист показывает в блоке защит.
 *
 * <p>Речь только о постоянных свойствах самого персонажа. Сюда не входит игнорирование
 * <i>чужого</i> сопротивления («Адепт стихий», «Отравитель», «Дар непреодолимого
 * нападения») — это свойство наносимого урона, а не защита, — и сопротивления с
 * условием: «Дар духа ночи» действует в тусклом свете, «Дар Отчаянной стойкости» — пока
 * персонаж окровавлен. Условные остаются в описании.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamageAffinity {
    @Schema(description = "Сопротивление урону")
    private Set<DamageType> resistances;

    @Schema(description = "Иммунитет к урону")
    private Set<DamageType> immunities;

    @Schema(description = "Уязвимость к урону")
    private Set<DamageType> vulnerabilities;

    /**
     * Сопротивление к типу урона, который игрок выбирает при взятии черты: «Отмеченный
     * драконом» выбирает один тип из пяти, «Дар устойчивости к энергиям» — два,
     * «Закалённая кожа» — дробящий или рубящий. Ссылается на {@code key} выбора из
     * {@code mechanics.choices}.
     */
    @Schema(description = "Ключ выбора типа урона, к которому даётся сопротивление",
            example = "damage-type")
    private String resistanceFromChoiceKey;
}
