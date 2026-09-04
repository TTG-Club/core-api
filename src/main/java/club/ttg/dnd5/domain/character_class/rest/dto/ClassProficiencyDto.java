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
public class ClassProficiencyDto {
    @Schema(description = "Владение доспехами", example = "Лёгкие и средние доспехи, щиты")
    private String armor;

    @Schema(description = "Владение оружием", example = "Простое и воинское оружие")
    private String weapon;

    @Schema(description = "Владение инструментами", example = "Ремесленные инструменты")
    private String tool;

    @Schema(description = "Владение навыками", example = "Выберите два навыка из следующих: Атлетика, Выживание...")
    private String skill;

    /**
     * Те же владения структурой, как их хранит запись: категориями словаря и выбором
     * навыков с количеством. Строки выше остаются для показа человеку, структура —
     * для потребителей, которые владения ПРИМЕНЯЮТ (лист персонажа), чтобы им не
     * приходилось разбирать русский текст обратно.
     */
    @Schema(description = "Владение доспехами категориями словаря")
    private ArmorProficiency armorData;

    @Schema(description = "Владение оружием категориями словаря")
    private WeaponProficiency weaponData;

    @Schema(description = "Выбор владения навыками: количество и пул")
    private SkillProficiency skillData;
}
