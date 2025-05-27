package club.ttg.dnd5.domain.common.rest.dto.select;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class KeySelectDto extends BaseSelectOptionDto {
    @Schema(description = "ключ к сущности")
    private String key;
}
