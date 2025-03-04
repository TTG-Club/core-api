package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@Schema(name = "Предметы, снаряжение и прочие объекты")
public class ItemDetailResponse extends BaseResponse {
    @Schema(name = "Типы объекта разделенные запятой")
    private String type;

    private Set<NameResponse> types;
    @Schema(name = "Категория доспеха")
    private String category;
    @JsonProperty(value = "ac")
    @Schema(name = "Класс доспеха")
    private String armorClass;
    @Schema(name = "Требуемая Сила для ношения без уменьшения скорости")
    private String strength;

    @Schema(name = "Помеха на проверку Ловкости (Скрытность)")
    private String stealth;

    /** Стоимость предмета */
    @Schema(name = "Стоимость")
    private String cost;
    /** Вес предмета */
    @Schema(name = "Вес")
    private String weight;
    // Магический предмет
    @Schema(name = "редкость магического предмета")
    private String rarity;
    @Schema(name = "уточнение типа магического предмета (например любой меч)")
    private String typeClarification;
    @Schema(name = "настройка магического предмета")
    private String attunement;
    @Schema(name = "количество зарядов магического предмета")
    private Byte charges;
    @Schema(name = "true если магический предмет - проклят")
    private Boolean curse;
}
