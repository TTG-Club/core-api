package club.ttg.dnd5.domain.character_class.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassEquipmentOptionDto {
    @Schema(description = "Метка варианта", example = "А")
    private String label;

    @Schema(description = "Предметы варианта")
    private List<ClassEquipmentItemDto> items;

    @Schema(description = "Количество монет", example = "15")
    private Integer coins;

    @Schema(description = "Сокращённое название монет", example = "зм")
    private String coin;
}
