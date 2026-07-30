package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class ItemShortResponse extends ShortResponse {
    @Schema(description = "Категория объекта", examples = {"ITEM", "ARMOR", "WEAPON"})
    private ItemCategory category;
    @Schema(description = "Типы объекта", examples = {"WEAPON", "MARTIAL_WEAPON", "MELEE_WEAPON"})
    private Collection<ItemType> types;
    @Schema(description = "Стоимость", examples = "1 зм")
    private String cost;
}
