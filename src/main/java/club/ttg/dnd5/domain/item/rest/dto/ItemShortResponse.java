package club.ttg.dnd5.domain.item.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class ItemShortResponse extends ShortResponse {
    @Schema(description = "Тип объекта", examples = {"ARMOR, WEAPON","OBJECT", "MAGIC"})
    private String type;
}
