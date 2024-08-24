package club.ttg.dnd5.dto.character;


import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dto.NameDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Builder
@Getter
@Setter
public class ClassRequest {
    @Schema(description = "уникальный url", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;
    @Schema(description = "названия", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameDto name;
    @Schema(description = "хит дайс", defaultValue = "к6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hitDice;

    @Schema(description = "Снаряжение", requiredMode = Schema.RequiredMode.REQUIRED)
    private String equipment;
    @Schema(description = "Владение доспехами")
    private String armorMastery;
    @Schema(description = "Владение оружием")
    private String weaponMastery;
    @Schema(description = "Владение инструментами")
    private String toolMastery;

    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private Collection<ClassFeatureDto> features;
    @Schema(description = "описание класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentUrl;
}
