package club.ttg.dnd5.dto.character;


import club.ttg.dnd5.dto.EntryDto;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.SourceDto;
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
    private EntryDto equipment;
    @Schema(description = "Владение доспехами")
    private EntryDto armorMastery;
    @Schema(description = "Владение оружием")
    private EntryDto weaponMastery;
    @Schema(description = "Владение инструментами")
    private EntryDto toolMastery;

    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private Collection<ClassFeatureDto> features;
    @Schema(description = "описание класса", requiredMode = Schema.RequiredMode.REQUIRED)
    private EntryDto description;
    @Schema(description = "описание класса на английском", requiredMode = Schema.RequiredMode.REQUIRED)
    private EntryDto original;
    @Schema(description = "умения класса", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentUrl;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceDto source;
}
