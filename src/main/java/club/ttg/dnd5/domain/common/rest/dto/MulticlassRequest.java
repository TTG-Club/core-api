package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MulticlassRequest {
    @JsonProperty("class")
    @Schema(description = "Url базового класса")
    private String url;
    @Schema(description = "URL базового подкласса")
    private String subclass;
    @Schema(description = "Уровень базового класса")
    private Integer level;
    @Schema(description = "Мультиклассы")
    private List<MulticlassDto> classes;
}
