package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.item.model.Armor;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemContainer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import club.ttg.dnd5.domain.item.model.ItemType;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

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
    @Schema(description = "Вес предмета в фунтах, дроби десятичными (¼ → 0.25). Пусто — вес не указан",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, examples = "0.25")
    private Double weight;

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

    /**
     * Инструмент
     */
    @Schema(description = "Инструмент")
    private Tool tool;

    /**
     * Категория снаряжения в вокабуляре VTTG. Пусто — выводится из типов предмета.
     */
    @Schema(description = "Категория снаряжения для виртуального стола",
            examples = {"adventurer-equipment", "trinket", "food", "potion"})
    private String equipmentCategory;

    /**
     * Расходуемый предмет: применение тратит единицу количества. Пусто — не
     * расходуется; боеприпас, яд и еда с эффектом расходуются и без галочки.
     */
    @Schema(description = "Расходуемый: применение тратит единицу предмета",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean consumable;

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

    /**
     * Активные эффекты предмета для экспорта в VTTG.
     *
     * <p>Перезаписываются ЦЕЛИКОМ: пустой список стирает прежние эффекты записи —
     * иначе удалённый в форме эффект пережил бы сохранение.</p>
     */
    @Schema(description = "Активные эффекты предмета для экспорта в VTTG")
    @Nullable
    @Valid
    private List<ActiveEffect> activeEffects;
}
