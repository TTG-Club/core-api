package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

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

    /**
     * Активные эффекты предмета в вокабуляре VTTG.
     *
     * <p>Отдаются вместе с деталью, а не только в «сыром» ответе мастерской: их считает
     * лист персонажа сайта — так же, как эффекты магического предмета. Пустой список в
     * ответ не пишется.</p>
     */
    @Schema(description = "Активные эффекты предмета")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ActiveEffect> activeEffects;
}
