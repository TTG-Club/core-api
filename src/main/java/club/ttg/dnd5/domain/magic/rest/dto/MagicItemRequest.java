package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.dictionary.item.magic.Rarity;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemRequest extends BaseRequest {
    @Schema(description = "Категория", examples = {"WEAPON", "ARMOR", "WAND"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MagicItemCategory category;

    @Schema(description = "Уточнение типа", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String typeClarification;

    @Schema(description = "Редкость", requiredMode = Schema.RequiredMode.REQUIRED)
    private Rarity rarity;

    @Schema(description = "Текстовое описание редкости", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String varies;

    @Schema(description = "Настройка", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Attunement attunement;

    @Schema(description = "Количество зарядов, если есть", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Byte charges;

    @Schema(description = "true если предмет проклят")
    private boolean curse;

    @Schema(description = "true если предмет расходуемый")
    private boolean consumable;
}
