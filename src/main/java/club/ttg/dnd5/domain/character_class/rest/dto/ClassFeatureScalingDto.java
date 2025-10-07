package club.ttg.dnd5.domain.character_class.rest.dto;


import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureScalingDto {
    @Schema(description = "Уровень улучшения", example = "5")
    private int level;

    @Schema(description = "Название улучшения", example = "Мультиатака (3)")
    private String name;

    @Schema(description = "Описание улучшения на этом уровне", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;

    @Schema(description = "Дополнительный текст рядом с уровнем", example = "Дополнительное использование")
    private String additional;

    @Schema(description = "Скрывать умение в подклассе")
    private boolean hideInSubclasses;

    public ClassFeatureScalingDto(ClassFeatureScaling classFeatureScaling) {
        this.level = classFeatureScaling.getLevel();
        this.name = classFeatureScaling.getName();
        this.description = classFeatureScaling.getDescription();
        this.additional = classFeatureScaling.getAdditional();
    }
}
