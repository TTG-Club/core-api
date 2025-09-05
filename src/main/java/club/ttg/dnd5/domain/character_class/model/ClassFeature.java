package club.ttg.dnd5.domain.character_class.model;

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
    private String description;

    @Schema(description = "Всплывающая подсказка для UI", example = "Один раз в короткий отдых")
    private String tooltip;

    @Schema(description = "Шкалирование особенности по уровням")
    List<ClassFeatureScaling> scaling;
}
