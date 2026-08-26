package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItemBonuses;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @Schema(description = "Бонусы поверх немагического предмета: к атаке, к урону и к КД",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemBonuses bonuses;

    @Schema(description = "Механика влияния на лист персонажа: условие применения, эффекты и заряды",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemMechanics mechanics;

    @Schema(description = "Дополнительный урон, который магия добавляет к броску немагической основы",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<DamagePart> damageParts;

    @Schema(description = "Количество зарядов, если есть", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Byte charges;

    @Schema(description = "true если предмет проклят")
    private boolean curse;

    @Schema(description = "true если предмет расходуемый")
    private boolean consumable;

    @Schema(description = "true если предмет — заклинательная фокусировка")
    private boolean focus;

    @Schema(description = "true если предмет адамантиновый")
    private boolean adamantine;

    @Schema(description = "URL связанных немагических предметов (для веса/стоимости при экспорте в VTTG и фильтрации)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> items;
}
