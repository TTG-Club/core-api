package club.ttg.dnd5.domain.magic.model.mechanics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Заряды магического предмета: сколько их всего и как они возвращаются.
 *
 * <p>Здесь только описание предмета из каталога. Текущий остаток зарядов — состояние
 * конкретного экземпляра и живёт на листе персонажа, а не в справочнике.</p>
 */
@Getter
@Setter
public class MagicItemResource {
    @Schema(description = "Максимум зарядов", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer maxCharges;

    @Schema(description = "Формула восстановления, например «1к6+4»",
            examples = {"1к6+4", "все"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String recharge;

    @Schema(description = "Когда заряды восстанавливаются", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemRechargeEvent rechargeEvent;

    @Schema(description = "Сколько зарядов тратит одно применение", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer cost;
}
