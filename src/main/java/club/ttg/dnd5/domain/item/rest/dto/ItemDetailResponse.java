package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@Schema(name = "Предметы, снаряжение и прочие объекты")
public class ItemDetailResponse extends BaseResponse {
    @Schema(description = "Категория объекта", examples = {"ITEM", "ARMOR", "WEAPON"})
    private String category;
    @Schema(description = "Типы объекта разделенные запятой", examples = "Оружие, Воинское оружие, Рукопашное оружие")
    private String types;
    /** Стоимость предмета */
    @Schema(description = "Стоимость", examples = "10 зм.")
    private String cost;
    /** Вес предмета */
    @Schema(description = "Вес", examples = "2 фунта")
    private String weight;

    @Schema(description = "Категория доспеха")
    private String armorCategory;
    @JsonProperty(value = "ac")
    @Schema(description = "Класс доспеха", examples = "12")
    private String armorClass;
    @Schema(description = "Требуемая Сила для ношения без уменьшения скорости", examples = "Сила 12")
    private String strength;
    @Schema(description = "Помеха на проверку Ловкости (Скрытность)", examples = "Есть")
    private String stealth;

    /** Оружие */
    @Schema(description = "Категория оружия", examples = "Простое рукопашное")
    private String weaponCategory;
    @Schema(description = "Урон оружия", examples = "1к6 дробящий")
    private String damage;
    @Schema(description = "Свойства оружия", examples = "Фехтовальное, Лёгкое, Метательное (дистанция 20/60)")
    private String weaponProperties;
    @Schema(description = "Приём оружия", examples = "Подавление")
    private String mastery;

    /**
     * Магический предмет
     */
    @Schema(description = "редкость магического предмета", examples = "редкий")
    private String rarity;
    @Schema(description = "уточнение типа магического предмета", examples = "любой меч")
    private String typeClarification;
    @Schema(description = "настройка магического предмета", examples = "Требуется настройка (волшебник)")
    private String attunement;
    @Schema(description = "количество зарядов магического предмета")
    private Byte charges;
    @Schema(description = "true если магический предмет - проклят")
    private Boolean curse;

    /**
     * Верховое животное
     */
    @Schema(description = "Переносимый вес", examples = "300 фунтов")
    private String carryingCapacity;

    /**
     * Характеристика
     */
    @Schema(description = "Характеристика для Тест к20", examples = "Сила")
    public String ability;
    @Schema(description = "Использование инструмента")
    public String uses;
    @Schema(description = "Что можно создать")
    public String creation;
}
