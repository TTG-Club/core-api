package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.dto.BaseDto;
import club.ttg.dnd5.domain.common.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
@Schema(name = "Предметы, снаряжение и прочие объекты")
public class ItemDetailResponse extends BaseDto {
    private Set<NameDto> types;
    /** Стоимость предмета */
    @Schema(name = "Стоимость")
    private String cost;
    /** Вес предмета */
    @Schema(name = "Вес")
    private String weight;

    @Schema(name = "true если магический, иначе false")
    private boolean magic = false;
    @Schema(name = "редкость магического предмета")
    private NameDto rarity;
    @Schema(name = "уточнение типа магического предмета (например любой меч)")
    private String typeClarification;
    @Schema(name = "настройка магического предмета")
    private String attunement;
    @Schema(name = "количество зарядов магического предмета")
    private Byte charges;
    @Schema(name = "true если магический предмет - проклят")
    private Boolean curse;
}
