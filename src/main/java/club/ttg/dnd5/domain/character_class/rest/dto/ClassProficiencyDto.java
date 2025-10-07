package club.ttg.dnd5.domain.character_class.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassProficiencyDto {
    @Schema(description = "Владение доспехами", example = "Лёгкие и средние доспехи, щиты")
    private String armor;

    @Schema(description = "Владение оружием", example = "Простое и воинское оружие")
    private String weapon;

    @Schema(description = "Владение инструментами", example = "Ремесленные инструменты")
    private String tool;

    @Schema(description = "Владение навыками", example = "Выберите два навыка из следующих: Атлетика, Выживание...")
    private String skill;
}
