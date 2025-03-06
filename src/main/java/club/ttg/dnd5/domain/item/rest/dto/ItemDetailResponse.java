package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.dictionary.item.WeaponCategory;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.item.model.WeaponMastery;
import club.ttg.dnd5.domain.item.model.WeaponProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@Schema(name = "Предметы, снаряжение и прочие объекты")
public class ItemDetailResponse extends BaseResponse {
    @Schema(name = "Категория объекта", examples = {"ITEM", "ARMOR", "WEAPON"})
    private String category;
    @Schema(name = "Типы объекта разделенные запятой", examples = "Оружие, Воинское оружие, Рукопашное оружие")
    private String types;
    /** Стоимость предмета */
    @Schema(name = "Стоимость", examples = "10 зм.")
    private String cost;
    /** Вес предмета */
    @Schema(name = "Вес", examples = "2 фунта")
    private String weight;

    @Schema(name = "Категория доспеха")
    private String armorCategory;
    @JsonProperty(value = "ac")
    @Schema(name = "Класс доспеха", examples = "12")
    private String armorClass;
    @Schema(name = "Требуемая Сила для ношения без уменьшения скорости", examples = "Сила 12")
    private String strength;
    @Schema(name = "Помеха на проверку Ловкости (Скрытность)", examples = "Есть")
    private String stealth;

    /** Оружие */
    @Schema(name = "Категория оружия", examples = "Простое рукопашное")
    private String weaponCategory;
    @Schema(name = "Урон", examples = "1к6 дробящий")
    private Short damage;

    private String weaponProperties;
    private Short range;
    private Short versatileDiceCount;
    private String versatileDice;
    private String mastery;

    /**
     * Магический предмет
     */
    @Schema(name = "редкость магического предмета", examples = "редкий")
    private String rarity;
    @Schema(name = "уточнение типа магического предмета", examples = "любой меч")
    private String typeClarification;
    @Schema(name = "настройка магического предмета", examples = "Требуется настройка (волшебник)")
    private String attunement;
    @Schema(name = "количество зарядов магического предмета")
    private Byte charges;
    @Schema(name = "true если магический предмет - проклят")
    private Boolean curse;

    /**
     * Верховое животное
     */
    @Schema(name = "Переносимый вес", examples = "300 фунтов")
    private String carryingCapacity;

    /**
     * Характеристика
     */
    @Schema(name = "Характеристика для Тест к20", examples = "Сила")
    public String ability;
    @Schema(name = "Использование инструмента")
    public String uses;
    @Schema(name = "Что можно создать")
    public String creation;
}
