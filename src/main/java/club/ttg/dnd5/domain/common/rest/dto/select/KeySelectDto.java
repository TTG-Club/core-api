package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
public class KeySelectDto extends BaseSelectOptionDto {
    @Schema(description = "ключ к сущности")
    private String key;
}
