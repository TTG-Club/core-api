package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.SourceDto;
import club.ttg.dnd5.model.base.HasTags;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ClassFeatureDto extends BaseDTO implements HasTags {
    @Schema(description = "С какого уровня доступно", requiredMode = Schema.RequiredMode.REQUIRED)
    private short level;
    private Map<String, String> tags;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceDto source;
}
