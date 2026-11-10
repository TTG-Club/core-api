package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.item.model.ItemContainer;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import com.fasterxml.jackson.annotation.JsonIgnore;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItemBonuses;
import club.ttg.dnd5.domain.magic.model.MagicItemTheme;
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

    @Schema(description = "Тематика", examples = {"ARMAMENT", "MAGIC", "RELIC", "TOOL"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemTheme theme;

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

    /**
     * Предмет-хранилище: вместимость в фунтах и невесомость содержимого. {@code null} —
     * не контейнер, пустой объект — контейнер без ограничения.
     *
     * <p>Сохранение меняет поле, только если запрос его прислал (хотя бы {@code null}):
     * форма, которая о поле не знает, не стирает разметку предмета.</p>
     */
    @Schema(description = "Хранилище: capacityWeight — вместимость в фунтах, weightless — содержимое не весит."
            + " null — не контейнер; поле не прислано — остаётся как было",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ItemContainer container;

    /** Прислал ли запрос поле {@link #container}: отсутствие поля не стирает его при сохранении. */
    @JsonIgnore
    @Schema(hidden = true)
    private boolean containerSet;

    public void setContainer(ItemContainer container) {
        this.container = container;
        this.containerSet = true;
    }

    @Schema(description = "true если предмет — заклинательная фокусировка")
    private boolean focus;

    @Schema(description = "true если предмет адамантиновый")
    private boolean adamantine;

    @Schema(description = "true если предмет показывается в списке раздела; по умолчанию true")
    private boolean showInList = true;

    @Schema(description = "URL связанных немагических предметов (для веса/стоимости при экспорте в VTTG и фильтрации)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> items;
}
