package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.magic.model.Attunement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemRequest extends BaseRequest {
    @Schema(description = "Категория", examples = {"WEAPON", "ARMOR", "WAND"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MagicItemCategoryRequest category;

    @Schema(description = "Редкость", requiredMode = Schema.RequiredMode.REQUIRED)
    private MagicItemRarityRequest rarity;

    @Schema(description = "Настройка", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Attunement attunement;

    @Schema(description = "Количество зарядов, если есть", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Byte charges;

    @Schema(description = "true если предмет проклят")
    private boolean curse;

    @Schema(description = "true если предмет расходуемый")
    private boolean consumable;
}
