package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureOptionRequest {

    @Schema(description = "Stable option slug", example = "agonizing_blast")
    private String key;

    @Schema(description = "Option name")
    private Name name;

    @Schema(description = "Option description")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Short additional label")
    private String additional;

    @Schema(description = "Option prerequisite")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String prerequisite;

    @Schema(description = "Required class level for this option")
    private Integer requiredClassLevel;

    @Schema(description = "Hide option in subclass and multiclass contexts")
    private boolean hideInSubclasses;
}
