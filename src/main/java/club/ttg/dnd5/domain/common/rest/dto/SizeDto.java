package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SizeDto {
    @Schema(description = "Размер")
    private Size size;
    @Schema(description = "дополнительное описание размера")
    private String suffix;
}
