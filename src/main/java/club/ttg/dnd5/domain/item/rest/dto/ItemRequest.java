package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.item.model.ItemCategory;
import club.ttg.dnd5.domain.item.model.ItemType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class ItemRequest extends BaseRequest {
    @Schema(name = "Типы объекта", requiredMode = Schema.RequiredMode.REQUIRED, examples = {"ITEM", "ARMOR", "WEAPON"})
    private ItemCategory category;
    @Schema(name = "Типы объекта разделенные запятой", examples = "Оружие, Воинское оружие, Рукопашное оружие")
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
    @Schema(name = "Категория оружия", examples = {"SIMPLE_MELEE", "SIMPLE_RANGED", "MATERIAL_MELEE", "MATERIAL_RANGED"})
    private WeaponCategory weaponCategory;
    @Schema(name = "Количество костей урона", examples = {"1", "2"})
    private byte diceCount;
    @Schema(name = "Кость урона", examples = "d6")
    private Dice diceDamage;
    @Schema(name = "Тип урона", examples = {"BLUDGEONING", "PIERCING", "SLASHING"})
    private DamageType damageType;
    @Schema(name = "Дистанция")
    private Range range;

    /**
     * Доспех
     */
    @Schema(name = "Категория доспеха", examples = "LIGHT")
    private ArmorCategory armorCategory;
    @JsonProperty(value = "ac")
    @Schema(name = "Класс доспеха", examples = "12")
    private String armorClass;
    @Schema(name = "Требуемая Сила для ношения без уменьшения скорости", examples = "Сила 12")
    private String strength;
    @Schema(name = "Помеха на проверку Ловкости (Скрытность)", examples = "Есть")
    private String stealth;

    /**
     * Верховое животное
     */
    @Schema(name = "Переносимый вес", examples = "300 фунтов")
    private String carryingCapacity;

    /**
     * Инструмент
     */
    @Schema(name = "Характеристика для Тест к20", examples = "STRENGTH")
    public Ability ability;
    @Schema(name = "Использование инструмента")
    public String uses;
    @Schema(name = "Что можно создать")
    public String creation;
}
