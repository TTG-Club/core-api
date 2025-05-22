package club.ttg.dnd5.domain.magic.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicItemRarityRequest {
    @Schema(description = "Редкость", requiredMode = Schema.RequiredMode.REQUIRED)
    private Rarity type;

    @Schema(description = "Текстовое описание редкости", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String varies;
}
