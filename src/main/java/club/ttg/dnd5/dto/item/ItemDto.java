package club.ttg.dnd5.dto.item;

import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.BaseDTO;
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
public class ItemDto extends BaseDTO {
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
    @Schema(name = "настройка магического предмета")
    private String attunement;
}
