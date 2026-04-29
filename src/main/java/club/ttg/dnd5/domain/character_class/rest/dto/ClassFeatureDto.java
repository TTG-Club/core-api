package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureDto {

    @Schema(description = "Является ли способность подклассовой")
    private Boolean isSubclass;

    @Schema(description = "Уникальный ключ особенности", example = "action_surge")
    private String key;

    @Schema(description = "Уровень получения особенности", example = "2")
    private int level;

    @Schema(description = "Название особенности", example = "Всплеск действий")
    private String name;

    @Schema(description = "Описание особенности", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String description;

    @Schema(description = "Дополнительный текст рядом с уровнем", example = "Дополнительное использование")
    private String additional;

    @Schema(description = "Масштабирование  особенности по уровням")
    List<ClassFeatureScalingDto> scaling;

    @Schema(description = "Options available for this feature")
    private List<ClassFeatureOptionDto> options;

    @Schema(description = "Скрывать умение в подклассе")
    private boolean hideInSubclasses;

    public ClassFeatureDto(ClassFeature classFeature, boolean isSubclass) {
        this(classFeature, isSubclass, isSubclass);
    }

    public ClassFeatureDto(ClassFeature classFeature, boolean isSubclass, boolean filterForSubclassContext) {
        this.isSubclass = isSubclass;
        this.key = classFeature.getKey();
        this.level = classFeature.getLevel();
        this.name = classFeature.getName();
        this.description = classFeature.getDescription();
        this.additional = classFeature.getAdditional();
        this.hideInSubclasses = classFeature.isHideInSubclasses();
        if (filterForSubclassContext) {
            this.scaling = Optional.ofNullable(classFeature.getScaling())
                    .orElse(List.of())
                    .stream()
                    .filter(featureScaling -> !featureScaling.isHideInSubclasses())
                    .map(ClassFeatureScalingDto::new)
                    .toList();
            this.options = Optional.ofNullable(classFeature.getOptions())
                    .orElse(List.of())
                    .stream()
                    .filter(option -> !option.isHideInSubclasses())
                    .map(ClassFeatureOptionDto::new)
                    .toList();
        } else {
            this.scaling = Optional.ofNullable(classFeature.getScaling())
                    .orElse(List.of())
                    .stream()
                    .map(ClassFeatureScalingDto::new)
                    .toList();
            this.options = Optional.ofNullable(classFeature.getOptions())
                    .orElse(List.of())
                    .stream()
                    .map(ClassFeatureOptionDto::new)
                    .toList();
        }
    }
}
