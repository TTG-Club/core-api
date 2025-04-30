package club.ttg.dnd5.domain.common.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlResponse {
    @Schema(description = "относительный url")
    private String url;
    @Schema(description = "текст ссылки")
    private String name;
    @Schema(description = "тип ссылки", examples = {"feat", "spell"})
    private String type;
}
