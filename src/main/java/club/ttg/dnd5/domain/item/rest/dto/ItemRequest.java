package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class ItemRequest extends BaseRequest {
    @Schema(description = "Типы объекта", requiredMode = Schema.RequiredMode.REQUIRED, examples = {"ITEM", "ARMOR", "WEAPON"})
    private ItemCategory category;
    @Schema(description = "Типы объекта разделенные запятой", examples = "Оружие, Воинское оружие, Рукопашное оружие")
    private Collection<ItemType> types;

    @Schema(description = "Стоимость предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer cost;
    @Schema(description = "Номинал монеты в стоимости", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Coin coin;
    @Schema(description = "Вес предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String weight;

    /**
     * Оружие
     */
    @Schema(description = "Оружие")
    private Weapon weapon;

    /**
     * Доспех
     */
    @Schema(description = "Доспех")
    private Armor armor;
}
