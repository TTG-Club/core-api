package club.ttg.dnd5.domain.common.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MulticlassLevelEntry {
    @JsonProperty("class")
    @Schema(description = "URL класса", example = "fighter")
    private String url;

    @Schema(description = "URL подкласса (если уровень >= 3)", example = "champion")
    private String subclass;

    @Schema(description = "Целевой накопленный уровень класса после этого сегмента (абсолютное значение)", example = "3")
    private Integer level;
}
