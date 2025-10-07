package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassProficiencyRequest {
    @Schema(description = "Владение доспехами")
    private ArmorProficiency armor;

    @Schema(description = "Владение оружием")
    private WeaponProficiency weapon;

    @Schema(description = "Владение инструментами",
            example = "Ремесленные инструменты, воровские инструменты")
    private String tool;

    @Schema(description = "Владение навыками")
    private SkillProficiency skill;
}
