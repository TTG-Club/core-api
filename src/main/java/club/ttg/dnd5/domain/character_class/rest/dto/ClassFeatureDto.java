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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassFeatureDto {

    @Schema(description = "Являетя ли способность подклассовой")
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

    @Schema(description = "Всплывающая подсказка для UI", example = "Один раз в короткий отдых")
    private String tooltip;

    @Schema(description = "Шкалирование особенности по уровням")
    List<ClassFeatureScalingDto> scaling;

    public ClassFeatureDto(ClassFeature classFeature, boolean isSubclass) {
        this.isSubclass = isSubclass;
        this.key = classFeature.getKey();
        this.level = classFeature.getLevel();
        this.name = classFeature.getName();
        this.description = classFeature.getDescription();
        this.tooltip = classFeature.getTooltip();
        this.scaling = classFeature.getScaling().stream().map(ClassFeatureScalingDto::new).toList();
    }

}
