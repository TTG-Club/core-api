package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
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
}
