package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemShortResponse extends ShortResponse {
    @Schema(description = "Тип объекта", examples = {"ARMOR, WEAPON","OBJECT"})
    private String type;
}
