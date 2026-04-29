package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureRequest;
import club.ttg.dnd5.dto.base.deserializer.MarkupDescriptionDeserializer;
import club.ttg.dnd5.util.SlugifyUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ClassFeature {

    @Schema(description = "Уникальный ключ особенности", example = "action_surge")
    private String key;

    @Schema(description = "Уровень получения особенности", example = "2")
    private int level;

    @Schema(description = "Название особенности", example = "Всплеск действий")
    private String name;

    @Schema(description = "Options catalog name for this feature", example = "Maneuvers")
    private String optionsName;

    @Schema(description = "Описание особенности")
    @JsonDeserialize(using = MarkupDescriptionDeserializer.class)
    private String description;

    @Schema(description = "Дополнительный текст рядом с уровнем", example = "Дополнительное использование")
    private String additional;

    @Schema(description = "Масштабирование особенности по уровням")
    List<ClassFeatureScaling> scaling;

    @Schema(description = "Опции класса доступные для умения")
    private List<ClassFeatureOption> options;

    @Schema(description = "Умение увеличивает характеристики")
    private boolean abilityImprovement;

    @Schema(description = "Скрывать умение в подклассе")
    private boolean hideInSubclasses;

    @Schema(description = "Бонус к увеличивает характеристик")
    private AbilityBonus abilityBonus;

    public ClassFeature(ClassFeatureRequest classFeatureRequest) {
        this.level = classFeatureRequest.getLevel();
        this.name = classFeatureRequest.getName();
        this.optionsName = classFeatureRequest.getOptionsName();
        this.description = classFeatureRequest.getDescription();
        this.additional = classFeatureRequest.getAdditional();
        this.scaling = classFeatureRequest.getScaling();
        this.options = Optional.ofNullable(classFeatureRequest.getOptions())
                .orElse(List.of())
                .stream()
                .map(ClassFeatureOption::new)
                .toList();
        this.key = SlugifyUtil.getSlug(this.name);
        this.hideInSubclasses = classFeatureRequest.isHideInSubclasses();
        this.abilityImprovement = classFeatureRequest.isAbilityImprovement();
        this.abilityBonus = classFeatureRequest.getAbilityBonus();
    }
}
