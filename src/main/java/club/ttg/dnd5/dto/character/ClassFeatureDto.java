package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dictionary.EntityType;
import club.ttg.dnd5.dto.EntryDto;
import club.ttg.dnd5.dto.NameDto;
import club.ttg.dnd5.dto.base.SourceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ClassFeatureDto {
    @Schema(description = "тип")
    private final String type = EntityType.FEATURE.getName();

    @Schema(description = "utl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @Schema(description = "название", requiredMode = Schema.RequiredMode.REQUIRED)
    private NameDto name;
    @Schema(description = "С какого уровня доступно", requiredMode = Schema.RequiredMode.REQUIRED)
    private int level;
    @Schema(description = "описание", requiredMode = Schema.RequiredMode.REQUIRED)
    private EntryDto entries;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceDto source;
}
