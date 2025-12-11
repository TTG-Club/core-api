package club.ttg.dnd5.domain.common.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MulticlassDto {
    @Schema(description = "Url мультикласса")
    private String url;
    @Schema(description = "URL мультиподкласса")
    private String subclass;
    @Schema(description = "Уровень мультикласса")
    private Integer level;
}
