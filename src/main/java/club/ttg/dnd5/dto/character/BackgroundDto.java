package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
@Schema(description = "Информация о происхождении")
public class BackgroundDto extends BaseDTO {
    @Schema(description = "Характеристики:")
    private Collection<NameDto> abilityScores;
    @Schema(description = "Черта")
    private String feat;
    @Schema(description = "Владение инструментом")
    private Collection<NameDto> skillProficiencies;
    @Schema(description = "Снаряжение")
    private String toolProficiency;
}
