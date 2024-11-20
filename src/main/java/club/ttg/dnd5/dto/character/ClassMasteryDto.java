package club.ttg.dnd5.dto.character;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
public class ClassMasteryDto {
    @Schema(description = "умение владения доспехами")
    private String armor;
    @Schema(description = "умение владения оружием")
    private String weapon;
    @Schema(description = "умение владения инструментами")
    private String tool;
    @Schema(description = "умение владения спасбросками")
    private Collection<String> savingThrow;
}
