package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CrlOptionDto extends BaseSelectOptionDto {
    @Schema(description = "Бонус мастерства")
    private int pb;
}
