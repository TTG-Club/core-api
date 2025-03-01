package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
public class ClassMasteryDto {
    @Schema(description = "Владения спасбросками")
    private String savingThrow;
    @Schema(description = "Владения навыками")
    private String skills;
    @Schema(description = "Владения оружием")
    private String weapon;
    @Schema(description = "Владения доспехами")
    private String armor;
    @Schema(description = "Владения инструментами")
    private String tool;
}
