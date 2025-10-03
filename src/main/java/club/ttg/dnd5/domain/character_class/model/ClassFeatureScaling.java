package club.ttg.dnd5.domain.character_class.model;


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
public class ClassFeatureScaling {
    @Schema(description = "Уровень улучшения", example = "5")
    private int level;

    @Schema(description = "Название улучшения", example = "Мультиатака (3)")
    private String name;

    @Schema(description = "Описание улучшения на этом уровне")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Дополнительный текст рядом с уровнем", example = "Дополнительное использование")
    private String additional;

    @Schema(description = "Скрывать умение в подклассе")
    private boolean hideInSubclasses;
}
