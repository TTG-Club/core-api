package club.ttg.dnd5.dto.character;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
public class ClassMasteryDto {
    @Schema(description = "владения доспехами")
    private String armor;
    @Schema(description = "владения оружием")
    private String weapon;
    @Schema(description = "владения инструментами")
    private String tool;
    @Schema(description = "владения спасбросками")
    private Set<String> savingThrow;
    @Schema(description = "количество доступных навыков для выбора")
    private short countAvailableSkills;
    @Schema(description = "список навыков")
    private Set<String> availableSkills;
}
