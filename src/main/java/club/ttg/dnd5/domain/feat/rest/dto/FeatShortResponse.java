package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Черта")
@Getter
@Setter
public class FeatShortResponse extends ShortResponse {
    @Schema(description = "Категория", examples = {"черта происхождения", "общая черта"})
    private String category;
}
