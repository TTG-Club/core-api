package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemCategoryRequest {
    private MagicItemCategory type;

    @Schema(description = "Уточнение типа", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String clarification;
}
