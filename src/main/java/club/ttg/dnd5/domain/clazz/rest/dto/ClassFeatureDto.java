package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.dto.base.HasTagDto;
import club.ttg.dnd5.dto.base.SourceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ClassFeatureDto extends BaseDto implements HasTagDto {
    @Schema(description = "С какого уровня доступно", requiredMode = Schema.RequiredMode.REQUIRED)
    private short level;
    private Set<String> tags;

    @Schema(description = "источник", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourceDto source;
}
