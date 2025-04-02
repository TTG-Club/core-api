package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
public class ClassFeatureResponse extends BaseResponse {
    @Schema(description = "С какого уровня доступно", requiredMode = Schema.RequiredMode.REQUIRED)
    private short level;

    @Schema(description = "Красивая фраза из книги", requiredMode = Schema.RequiredMode.REQUIRED)
    private String quote;
}
