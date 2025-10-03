package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.util.SlugifyUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
public class ClassFeature {

    @Schema(description = "Уникальный ключ особенности", example = "action_surge")
    private String key;

    @Schema(description = "Уровень получения особенности", example = "2")
    private int level;

    @Schema(description = "Название особенности", example = "Всплеск действий")
    private String name;

    @Schema(description = "Описание особенности")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Дополнительный текст рядом с уровнем", example = "Дополнительное использование")
    private String additional;

    @Schema(description = "Масштабирование особенности по уровням")
    List<ClassFeatureScaling> scaling;

    @Schema(description = "Скрывать умение в подклассе")
    private boolean hideInSubclasses;

    public ClassFeature(ClassFeatureRequest classFeatureRequest) {
        this.level = classFeatureRequest.getLevel();
        this.name = classFeatureRequest.getName();
        this.description = classFeatureRequest.getDescription();
        this.additional = classFeatureRequest.getAdditional();
        this.scaling = classFeatureRequest.getScaling();
        this.key = SlugifyUtil.getSlug(this.name);
        this.hideInSubclasses = classFeatureRequest.isHideInSubclasses();
    }
}
